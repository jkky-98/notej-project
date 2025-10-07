package me.notej.notej_api.core.category.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.core.blogmain.repository.BlogRepository;
import me.notej.notej_api.core.category.domain.Category;
import me.notej.notej_api.core.category.dto.CategoryAllResponse;
import me.notej.notej_api.core.category.dto.CategoryCreateRequest;
import me.notej.notej_api.core.category.dto.CategoryResponse;
import me.notej.notej_api.core.category.dto.CategoryUpdateRequest;
import me.notej.notej_api.core.category.repository.CategoryRepository;
import me.notej.notej_api.core.member.domain.Member;
import me.notej.notej_api.core.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final BlogRepository blogRepository;

    @Transactional(readOnly = true)
    public CategoryAllResponse getCategories(final String blogUrl) {
        List<Category> categories = categoryRepository.findAllByBlog_Url(blogUrl);

        List<CategoryResponse> categoryResponses = categories.stream()
                .sorted(Comparator.comparing(Category::getSeq)) // seq 기준으로 정렬해야 프론트엔드가 그대로 사용해서 카테고리 랜더링 정렬가능
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getParent() != null ? category.getParent().getId() : null,
                        category.getName(),
                        category.getSeq()
                ))
                .toList();

        return new CategoryAllResponse(categoryResponses);
    }

    @Transactional(readOnly = true)
    public CategoryAllResponse getCategories(final Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String memberUuid = user.getUsername();

        Member member = memberRepository.findByMemberUuid(memberUuid).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Blog blog = member.getBlog();
        List<Category> categories = categoryRepository.findAllByBlog(blog);

        List<CategoryResponse> categoryResponses = categories.stream()
                .sorted(Comparator.comparing(Category::getSeq)) // seq 기준으로 정렬해야 프론트엔드가 그대로 사용해서 카테고리 랜더링 정렬가능
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getParent() != null ? category.getParent().getId() : null,
                        category.getName(),
                        category.getSeq()
                ))
                .toList();

        return new CategoryAllResponse(categoryResponses);
    }

    @Transactional
    public void createCategory(final String blogUrl, final CategoryCreateRequest request) {

        Blog blog = blogRepository.findByUrl(blogUrl).orElseThrow(() -> new EntityNotFoundException("BLOG_NOT_FOUND"));

        // 1. 부모 카테고리 찾기
        Category parent = null; // 기본적으로 parent는 null로 초기화

        // parentId가 요청에 있다면, 부모 카테고리 조회 및 설정
        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("CATEGORY[Parent]_NOT_FOUND"));
        }
        // 2. 해당 부모를 가지는(혹은 최상위) 카테고리 중 가장 높은 seq 값을 찾아서 +1 하거나, 1로 설정
        //    parentId가 null이면 최상위 카테고리 중, null이 아니면 해당 parentId를 가진 카테고리 중 가장 높은 seq 찾기
        Integer maxSeq = categoryRepository.findMaxSeqByParentId(blog.getId(), request.parentId());

        int newSeq = (maxSeq != null) ? maxSeq + 1 : 1; // maxSeq가 없으면 1, 있으면 +1

        // 3. Category 엔티티 생성 및 저장
        Category newCategory = Category.builder()
                .blog(blog)
                .name(request.name())
                .seq(newSeq) // 계산된 seq 값 사용
                .parent(parent)
                .build();

        categoryRepository.save(newCategory);
        log.info("[CategoryService][saveCategory] CategoryService - saveCategory() category : {}", newCategory);
    }

    @Transactional
    public void updateCategories(List<CategoryUpdateRequest> requests, final String blogUrl) { // List로 받기
        for (CategoryUpdateRequest request : requests) { // 리스트 순회하면서 각각 업데이트
            Long categoryId = request.categoryId();

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("CATEGORY_NOT_FOUND: " + categoryId));

            category.setSeq(request.seq());

            // parentId가 null이면 최상위 카테고리로 간주 (혹은 특정 root값)
            // null이 아니면 해당 parentId를 가진 부모 카테고리 찾아서 설정
            if (request.parentId() != null) {
                Long parentId = request.parentId();
                if (parentId.equals(categoryId)) { // 자기 자신을 부모로 설정 방지
                    throw new IllegalArgumentException("카테고리는 자기 자신을 부모로 가질 수 없습니다: " + categoryId);
                }
                Category categoryParent = categoryRepository.findById(parentId)
                        .orElseThrow(() -> new EntityNotFoundException("PARENT_CATEGORY_NOT_FOUND: " + parentId));
                category.setParent(categoryParent);
            } else {
                category.setParent(null); // parentId가 null이면 부모가 없도록 설정
            }

            // 엔티티가 변경되었으므로 자동 더티 체킹으로 저장됨
            log.info("[CategoryService][updateCategory] Category ID: {} updated to seq: {}, parent: {}",
                    categoryId, category.getSeq(), category.getParent() != null ? category.getParent().getId() : "null");
        }
    }
}
