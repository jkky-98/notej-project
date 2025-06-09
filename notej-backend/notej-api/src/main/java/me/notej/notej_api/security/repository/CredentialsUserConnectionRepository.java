package me.notej.notej_api.security.repository;

import me.notej.notej_api.security.domain.CredentialsUserConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsUserConnectionRepository extends JpaRepository<CredentialsUserConnection, Long> {
    Optional<CredentialsUserConnection> findByMemberEmail(String email);
    Optional<CredentialsUserConnection> findByMember_MemberUuid(String memberUUID);
}
