package me.notej.notej_api.security.refresh.domain;

import jakarta.persistence.*;
import lombok.*;
import me.notej.notej_api.core.member.domain.Member;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public void updateToken(String token, Instant expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public void revoke() {
        this.revoked = true;
    }
}
