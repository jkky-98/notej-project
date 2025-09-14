package me.notej.notej_api.security.credentials.domain;

import jakarta.persistence.*;
import lombok.*;
import me.notej.notej_api.core.member.domain.Member;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CredentialsUserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String passwordHash;


}
