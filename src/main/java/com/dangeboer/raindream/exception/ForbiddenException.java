package com.dangeboer.raindream.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("当前用户无权限");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
