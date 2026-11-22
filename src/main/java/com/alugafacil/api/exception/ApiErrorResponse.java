package com.alugafacil.api.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		List<FieldErrorDetail> fieldErrors
) {

	public record FieldErrorDetail(String field, String message) {
	}
}
