package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.PostHits;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHitsRepository extends JpaRepository<PostHits, Long> {
}
