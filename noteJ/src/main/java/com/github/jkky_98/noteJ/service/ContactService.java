package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Contact;
import com.github.jkky_98.noteJ.domain.mapper.ContactMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.ContactRepository;
import com.github.jkky_98.noteJ.web.controller.form.ContactForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserService userService;

    @Transactional
    public void addContact(ContactForm form, Optional<User> sessionUser) {
        Contact contact = sessionUser
                .map(user -> ContactMapper.INSTANCE.toContactByContactForm(form, userService.findUserById(user.getId())))
                .orElseGet(() -> ContactMapper.INSTANCE.toContactByContactForm(form, null));

        contactRepository.save(contact);
    }
}
