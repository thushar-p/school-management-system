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

import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.exception.AdminAlreadyFoundException;
import com.school.sba.exception.AdminCannotBeAssignedToAcademicProgram;
import com.school.sba.exception.AdminNotFoundException;
import com.school.sba.exception.OnlyAdminCanCreateSchoolException;
import com.school.sba.exception.OnlyTeacherCanBeAssignedToSubjectException;
import com.school.sba.exception.ScheduleAlreadyPresentException;
import com.school.sba.exception.ScheduleNotFoundException;
import com.school.sba.exception.SchoolCannotBeCreatedException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.exception.SubjectNotFoundException;
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
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "school is already present");
	}
	
	@ExceptionHandler(UserNotFoundByIdException.class)
	public ResponseEntity<Object> handleUserNotFoundByIdException(UserNotFoundByIdException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "user not found by id by in database");
	}
	
	@ExceptionHandler(ScheduleAlreadyPresentException.class)
	public ResponseEntity<Object> handleScheduleAlreadyPresentException(ScheduleAlreadyPresentException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "Schedule is already present and assigned to school");
	}
	
	@ExceptionHandler(ScheduleNotFoundException.class)
	public ResponseEntity<Object> handleScheduleNotFoundException(ScheduleNotFoundException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "Schedule not found, Try adding the schedule first");
	}
	
	@ExceptionHandler(AcademicProgramNotFoundException.class)
	public ResponseEntity<Object> handleAcademicProgramNotFoundException(AcademicProgramNotFoundException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "Academic program not found, Try adding the acadamic first");
	}
	
	@ExceptionHandler(AdminNotFoundException.class)
	public ResponseEntity<Object> handleAdminNotFoundException(AdminNotFoundException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "admin not found");
	}
	
	@ExceptionHandler(OnlyAdminCanCreateSchoolException.class)
	public ResponseEntity<Object> handleOnlyAdminCanCreateSchoolException(OnlyAdminCanCreateSchoolException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "only admin is able to create school");
	}
	
	@ExceptionHandler(AdminCannotBeAssignedToAcademicProgram.class)
	public ResponseEntity<Object> handleAdminCannotBeAssignedToAcademicProgram(AdminCannotBeAssignedToAcademicProgram exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "admin cannot be assigned to academic programs");
	}
	
	@ExceptionHandler(SubjectNotFoundException.class)
	public ResponseEntity<Object> handleSubjectNotFoundException(SubjectNotFoundException exception) {
		return structure(HttpStatus.NOT_FOUND, exception.getMessage(), "subject not found by id");
	}

	@ExceptionHandler(OnlyTeacherCanBeAssignedToSubjectException.class)
	public ResponseEntity<Object> handleOnlyTeacherCanBeAssignedToSubjectException(OnlyTeacherCanBeAssignedToSubjectException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "subject can only assigned to  teacher");
	}
	
	@ExceptionHandler(AdminAlreadyFoundException.class)
	public ResponseEntity<Object> handleAdminAlreadyFoundException(AdminAlreadyFoundException exception) {
		return structure(HttpStatus.BAD_REQUEST, exception.getMessage(), "admin is already inserted");
	}
}
