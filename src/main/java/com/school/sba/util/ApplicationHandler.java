package com.school.sba.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.school.sba.exception.AdminAlreadyExistException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.exception.UserNotFoundIdException;

@RestControllerAdvice
public class ApplicationHandler extends ResponseEntityExceptionHandler {

	public ResponseEntity<Object> structure(HttpStatus status, String message, Object rootCause) {
		return new ResponseEntity<Object>(Map.of("status", status.value(), "message", message, "root cause", rootCause),
				status);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<ObjectError> allErrors = ex.getAllErrors();

		Map<String, String> errors = new HashMap<String, String>();

		allErrors.forEach(error -> {

			FieldError fieldError = (FieldError) error;
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());

		});

		return structure(HttpStatus.BAD_REQUEST, "Failed to save the data", errors);

	}

	@ExceptionHandler(SchoolNotFoundByIdException.class)
	public ResponseEntity<Object> handleSchoolNotFoundByIdException(SchoolNotFoundByIdException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "school not found by id in database");
	}

	@ExceptionHandler(AdminAlreadyExistException.class)
	public ResponseEntity<Object> handleAdminAlreadyExistException(AdminAlreadyExistException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "Admin is already present in database");
	}
	
	@ExceptionHandler(UserNotFoundIdException.class)
	public ResponseEntity<Object> handleUserNotFoundByIdException(UserNotFoundIdException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "use not found by id by in database");
	}

}
