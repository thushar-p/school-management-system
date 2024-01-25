package com.school.sba.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubjectCannotBeAssignedToStudentException extends RuntimeException {
	private String message;

}
