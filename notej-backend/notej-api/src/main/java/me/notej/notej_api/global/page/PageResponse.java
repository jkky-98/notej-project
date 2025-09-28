package me.notej.notej_api.global.page;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;        // 현재 페이지의 데이터 목록
    private int page;               // 현재 페이지 번호
    private int size;               // 페이지당 항목 수
    private long totalElements;     // 전체 항목 수
    private int totalPages;         // 전체 페이지 수
    private boolean first;          // 첫 페이지 여부
    private boolean last;           // 마지막 페이지 여부
    private boolean hasNext;        // 다음 페이지 존재 여부

    // 생성자
    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = page == 1;
        this.last = page == totalPages || totalPages == 0;
        this.hasNext = !last;
    }

    // 스프링의 Page 객체로부터 변환하는 정적 팩토리 메서드
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,  // Page는 0부터 시작하므로 1 추가
                page.getSize(),
                page.getTotalElements()
        );
    }

    // 무한 스크롤에 최적화된 간소화 버전 (totalElements, totalPages 없음)
    public static <T> PageResponse<T> forInfiniteScroll(List<T> content, int page, int size, boolean hasNext) {
        PageResponse<T> response = new PageResponse<>(content, page, size, 0);
        response.hasNext = hasNext;
        response.last = !hasNext;
        return response;
    }

    // getter 및 setter 메서드
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}
