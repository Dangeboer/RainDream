package com.dangeboer.raindream.exception;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException() {
        super("资源已存在");
    }

    public DuplicateKeyException(String message) {
        super(message);
    }
}
