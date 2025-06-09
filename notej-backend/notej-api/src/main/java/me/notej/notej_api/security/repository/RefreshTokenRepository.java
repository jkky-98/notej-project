package me.notej.notej_api.security.repository;

import me.notej.notej_api.security.domain.Member;
import me.notej.notej_api.security.refresh.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMemberId(Long memberId);
    Optional<RefreshToken> findByToken(String refreshToken);
    void deleteByMember(Member member);
    List<RefreshToken> findAllByMemberAndRevokedFalse(Member member);
    Optional<RefreshToken> findByMember_MemberUuid(String memberUuid);
    Optional<RefreshToken> findTopByMemberIdAndRevokedFalseOrderByExpiryDateDesc(Long memberId);

}
