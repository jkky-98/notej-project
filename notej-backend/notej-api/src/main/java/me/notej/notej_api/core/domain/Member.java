package me.notej.notej_api.core.domain;

import jakarta.persistence.*;
import lombok.*;
import me.notej.notej_api.core.blogmain.domain.Blog;
import me.notej.notej_api.global.baseentity.BaseTimeEntity;
import me.notej.notej_api.security.oauth2.user.OAuth2Provider;
import me.notej.notej_api.security.oauth2.domain.OAuth2UserConnection;
import me.notej.notej_api.security.common.Role;
import me.notej.notej_api.security.refresh.domain.RefreshToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String memberUuid;

    private String nickname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", unique = true)
    private Blog blog;

    // -------------------------------------------------------
    // 역할(role) 컬렉션: Set<Role>
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "member_roles",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));
    // -------------------------------------------------------

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuth2UserConnection> connections = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Builder.Default
    private boolean completed = false;

    public void addConnection(OAuth2UserConnection conn) {
        connections.add(conn);
        conn.setMember(this);
    }

    public void removeConnection(OAuth2Provider provider, String providerId) {
        connections.removeIf(c -> c.getProvider() == provider && c.getProviderId().equals(providerId));
    }

    /**
     * OAuth2 로그인 시 프로필 정보 갱신용 메서드
     * @param name     공급자로부터 전달된 최신 이름
     * @param nickname 공급자로부터 전달된 최신 닉네임
     * @return this   변경된 엔티티 반환
     */
    public Member updateName(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
        return this;
    }
}
