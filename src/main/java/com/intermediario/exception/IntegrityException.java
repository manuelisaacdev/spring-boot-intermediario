package com.intermediario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class IntegrityException extends RuntimeException {
	public IntegrityException(String message) {
		super(message);
	}
}
