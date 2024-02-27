package com.intermediario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorRequestException extends RuntimeException {
	public InternalServerErrorRequestException(String message) {
		super(message);
	}
	public InternalServerErrorRequestException(Throwable throwable) {
		super(throwable);
	}
}
