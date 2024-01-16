package com.school.sba.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.sba.entity.School;
import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.service.SchoolService;
import com.school.sba.util.ResponseStructure;

@RestController
@RequestMapping("/schools")
public class SchoolController {
	
	@Autowired
	private SchoolService schoolService;
		
	@PostMapping
	public ResponseEntity<ResponseStructure<School>> saveSchool(@RequestBody SchoolRequest schoolRequest){
		return schoolService.saveSchool(schoolRequest);
	}
	
	@DeleteMapping("/{schoolId}")
	public ResponseEntity<ResponseStructure<School>> deleteSchool(@PathVariable Integer schoolId){
		return schoolService.deleteSchool(schoolId);
	}
	
	@PutMapping("/{schoolId}")
	public ResponseEntity<ResponseStructure<School>> updateSchool(@PathVariable Integer schoolId, @RequestBody SchoolRequest schoolRequest){
		return schoolService.updateSchool(schoolId, schoolRequest);
	}
	
	@GetMapping("/{schoolId}")
	public ResponseEntity<ResponseStructure<School>> findSchool(@PathVariable Integer schoolId){
		return schoolService.findSchool(schoolId);
	}

	@GetMapping
	public ResponseEntity<ResponseStructure<List<School>>> findAllSchool(){
		return schoolService.findAllSchool();
	}
	
}
