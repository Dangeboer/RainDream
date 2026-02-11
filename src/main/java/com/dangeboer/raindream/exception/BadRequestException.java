package com.dangeboer.raindream.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super("请求参数不合法");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
