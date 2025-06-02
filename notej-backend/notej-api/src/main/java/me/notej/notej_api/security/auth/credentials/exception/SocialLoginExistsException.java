package me.notej.notej_api.security.auth.credentials.exception;

public class SocialLoginExistsException extends RuntimeException {
    public SocialLoginExistsException(String message) {
        super(message);
    }
}
