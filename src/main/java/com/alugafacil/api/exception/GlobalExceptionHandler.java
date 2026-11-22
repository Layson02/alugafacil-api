package com.alugafacil.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(
			ConflictException ex,
			HttpServletRequest request
	) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ApiErrorResponse.FieldErrorDetail(
						error.getField(),
						error.getDefaultMessage()
				))
				.toList();

		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Erro de validação nos dados enviados",
				request,
				fieldErrors
		);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request
	) {
		List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations()
				.stream()
				.map(violation -> new ApiErrorResponse.FieldErrorDetail(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.toList();

		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Erro de validação nos dados enviados",
				request,
				fieldErrors
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	) {
		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Corpo da requisição inválido ou malformado",
				request,
				List.of()
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException ex,
			HttpServletRequest request
	) {
		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Parâmetro inválido: " + ex.getName(),
				request,
				List.of()
		);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
			DataIntegrityViolationException ex,
			HttpServletRequest request
	) {
		return buildResponse(
				HttpStatus.CONFLICT,
				"Operação viola restrição de integridade dos dados",
				request,
				List.of()
		);
	}

	private ResponseEntity<ApiErrorResponse> buildResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			List<ApiErrorResponse.FieldErrorDetail> fieldErrors
	) {
		ApiErrorResponse body = new ApiErrorResponse(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors
		);
		return ResponseEntity.status(status).body(body);
	}
}
