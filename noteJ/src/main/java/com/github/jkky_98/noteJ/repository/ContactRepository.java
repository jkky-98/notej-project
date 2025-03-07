package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
