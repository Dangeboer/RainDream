package com.dangeboer.raindream.exception;

public class CanNotFoundException extends RuntimeException {
    public CanNotFoundException() {
        super("资源不存在");
    }

    public CanNotFoundException(String message) {
        super(message);
    }
}
