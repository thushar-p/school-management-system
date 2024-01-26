package com.school.sba.service;

import org.springframework.http.ResponseEntity;

import com.school.sba.util.ResponseStructure;

public interface ClassHourService {

	ResponseEntity<ResponseStructure<String>> addClassHour(int programId);

}
