package com.github.jkky_98.noteJ.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PostValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidPost() {
        // Given
        Post post = Post.builder()
                .title("Valid Title")
                .content("This is a valid content with more than 10 characters.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(false)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    public void testTitleBlank() {
        // Given
        Post post = Post.builder()
                .title("") // Blank title
                .content("This is valid content.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(true)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title must be between 3 and 255 characters.");
    }

    @Test
    public void testTitleTooShort() {
        // Given
        Post post = Post.builder()
                .title("Hi") // Title too short
                .content("This is valid content.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(true)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title must be between 3 and 255 characters.");
    }

    @Test
    public void testContentBlank() {
        // Given
        Post post = Post.builder()
                .title("Valid Title")
                .content("") // Blank content
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(true)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Content is required.");
    }

    @Test
    public void testContentTooShort() {
        // Given
        Post post = Post.builder()
                .title("Valid Title")
                .content("Short") // Content too short
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(true)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Content must be at least 10 characters.");
    }

    @Test
    public void testThumbnailInvalidUrl() {
        // Given
        Post post = Post.builder()
                .title("Valid Title")
                .content("This is valid content.")
                .thumbnail("invalid-thumbnail-url") // Invalid URL
                .is_private(true)
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("thumbnail");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Thumbnail must be a valid URL.");
    }

    @Test
    public void testPrivacyStatusNull() {
        // Given
        Post post = Post.builder()
                .title("Valid Title")
                .content("This is valid content.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(null) // Privacy status is null
                .build();

        // When
        Set<ConstraintViolation<Post>> violations = validator.validate(post);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("is_private");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Privacy status must be specified.");
    }
}
