package com.tasknest.exception;

public class InvalidTaskMoveException extends RuntimeException {
    public InvalidTaskMoveException(String message) {
        super(message);
    }
}
