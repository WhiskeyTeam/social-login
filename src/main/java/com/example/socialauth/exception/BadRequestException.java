package com.example.socialauth.exception;

/**
 * 잘못된 요청 예외를 나타내는 클래스입니다.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
