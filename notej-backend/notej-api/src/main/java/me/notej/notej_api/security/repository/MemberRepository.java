package me.notej.notej_api.security.repository;

import me.notej.notej_api.security.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberUuid(String memberUuid);

    boolean existsByBlogUrl(String blogUrl);
}
