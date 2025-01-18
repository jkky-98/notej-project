package com.github.jkky_98.noteJ.exception;

public class LikeBadRequestClientException extends LikeClientException{

    private static final String ERROR_CODE = "LIKE_BAD_REQUEST";

    public LikeBadRequestClientException(String message) {
        super(message, ERROR_CODE);
    }
}
