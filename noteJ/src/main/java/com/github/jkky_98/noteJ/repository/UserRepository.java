package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
