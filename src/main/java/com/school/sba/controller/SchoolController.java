package com.school.sba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.responsedto.SchoolResponse;
import com.school.sba.service.SchoolService;
import com.school.sba.util.ResponseStructure;

@RestController
public class SchoolController {
	
	@Autowired
	private SchoolService schoolService;
		
	@PostMapping("users/{userId}/schools")
	public ResponseEntity<ResponseStructure<SchoolResponse>> createSchool(@PathVariable("userId") int userId, @RequestBody SchoolRequest schoolRequest){
		return schoolService.createSchool(userId, schoolRequest);
	}
	
	@DeleteMapping("/schools/{schoolId}")
	public ResponseEntity<ResponseStructure<SchoolResponse>> deleteSchool(@PathVariable Integer schoolId){
		return schoolService.deleteSchool(schoolId);
	}
	
	@PutMapping("/schools/{schoolId}")
	public ResponseEntity<ResponseStructure<SchoolResponse>> updateSchool(@PathVariable Integer schoolId, @RequestBody SchoolRequest schoolRequest){
		return schoolService.updateSchool(schoolId, schoolRequest);
	}
	
	@GetMapping("/schools/{schoolId}")
	public ResponseEntity<ResponseStructure<SchoolResponse>> findSchool(@PathVariable Integer schoolId){
		return schoolService.findSchool(schoolId);
	}

	
}
