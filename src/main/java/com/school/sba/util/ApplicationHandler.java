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

import com.school.sba.exception.SchoolCannotBeCreatedException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.exception.UserNotFoundByIdException;

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

	@ExceptionHandler(SchoolCannotBeCreatedException.class)
	public ResponseEntity<Object> handleAdminAlreadyExistException(SchoolCannotBeCreatedException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "school cannot be created beacuse admin is not present");
	}
	
	@ExceptionHandler(UserNotFoundByIdException.class)
	public ResponseEntity<Object> handleUserNotFoundByIdException(UserNotFoundByIdException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "use not found by id by in database");
	}

}
