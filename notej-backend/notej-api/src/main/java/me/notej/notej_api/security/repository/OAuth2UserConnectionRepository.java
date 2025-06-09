package me.notej.notej_api.security.repository;

import me.notej.notej_api.security.auth.oauth2.user.OAuth2Provider;
import me.notej.notej_api.security.domain.Member;
import me.notej.notej_api.security.domain.OAuth2UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2UserConnectionRepository extends JpaRepository<OAuth2UserConnection, Long> {
    Optional<OAuth2UserConnection> findByProviderAndProviderId(OAuth2Provider provider, String providerId);
    void deleteByMemberAndProvider(Member member, OAuth2Provider provider);
}
