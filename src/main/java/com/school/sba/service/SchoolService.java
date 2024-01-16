package com.school.sba.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.school.sba.entity.School;
import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.util.ResponseStructure;

public interface SchoolService {

	ResponseEntity<ResponseStructure<School>> saveSchool(SchoolRequest schoolRequest);
	
	ResponseEntity<ResponseStructure<School>> deleteSchool(Integer schoolId);

	ResponseEntity<ResponseStructure<School>> updateSchool(Integer schoolId, SchoolRequest schoolRequest);

	ResponseEntity<ResponseStructure<School>> findSchool(Integer schoolId);

	ResponseEntity<ResponseStructure<List<School>>> findAllSchool();

}
