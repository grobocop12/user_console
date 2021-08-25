package com.grobocop.userconsole.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RefreshException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RefreshException(String message) {
        super(message);
    }
}
