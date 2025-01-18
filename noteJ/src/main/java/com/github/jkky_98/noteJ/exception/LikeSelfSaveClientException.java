package com.github.jkky_98.noteJ.exception;

public class LikeSelfSaveClientException extends LikeClientException {

    private static final String ERROR_CODE = "LIKE_SELF_SAVE_ERROR";

    public LikeSelfSaveClientException(String message) {
        super(message, ERROR_CODE);
    }
}
