package com.school.sba.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.School;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.repository.ISchoolRepository;
import com.school.sba.request.SchoolRequest;
import com.school.sba.service.ISchoolService;
import com.school.sba.util.ResponseStructure;


@Service
public class SchoolServiceImpl implements ISchoolService{

	@Autowired
	private ISchoolRepository schoolRepo;

	@Autowired
	private ResponseStructure<School> responseStructure;

	
	private School mapToUser(SchoolRequest schoolRequest) {
		return School.builder()
				.schoolName(schoolRequest.getSchoolName())
				.schoolEmailId(schoolRequest.getSchoolEmailId())
				.schoolContactNumber(schoolRequest.getSchoolContactNumber())
				.schoolAddress(schoolRequest.getSchoolAddress())
				.build();
	}
	
	@Override
	public ResponseEntity<ResponseStructure<School>> saveSchool(SchoolRequest schoolRequest){

		School saveStudent = schoolRepo.save(mapToUser(schoolRequest));

		responseStructure.setStatus(HttpStatus.CREATED.value());
		responseStructure.setMessage("School data inserted successfully");
		responseStructure.setData(saveStudent);

		return new ResponseEntity<ResponseStructure<School>>(responseStructure, HttpStatus.CREATED);

	}



	@Override
	public ResponseEntity<ResponseStructure<School>> deleteSchool(Integer schoolId) {

		School existingSchool = schoolRepo.findById(schoolId)
				.orElseThrow(() -> new SchoolNotFoundByIdException("school object cannot be deleted due to absence of school id"));

		schoolRepo.deleteById(schoolId);

		responseStructure.setStatus(HttpStatus.OK.value());
		responseStructure.setMessage("School data deleted successfully from database");
		responseStructure.setData(existingSchool);

		return new ResponseEntity<ResponseStructure<School>>(responseStructure, HttpStatus.OK);
	}
	
	

	@Override
	public ResponseEntity<ResponseStructure<School>> updateSchool(Integer schoolId, SchoolRequest schoolRequest)
			throws SchoolNotFoundByIdException {

		School existingSchool = schoolRepo.findById(schoolId)
				.map(u -> {
					School school = mapToUser(schoolRequest);
					school.setSchoolId(schoolId);
					return schoolRepo.save(school);
				})
				.orElseThrow(() -> new SchoolNotFoundByIdException("school object cannot be updated due to absence of technical problems"));


		responseStructure.setStatus(HttpStatus.OK.value());
		responseStructure.setMessage("School data updated successfully in database");
		responseStructure.setData(existingSchool);

		return new ResponseEntity<ResponseStructure<School>>(responseStructure, HttpStatus.OK);

	}
	
	

	@Override
	public ResponseEntity<ResponseStructure<School>> findSchool(Integer schoolId)
			throws SchoolNotFoundByIdException {

		School fetchedSchool = schoolRepo.findById(schoolId)
				.orElseThrow(() -> new SchoolNotFoundByIdException("School object cannot be fetched because it is not present in DB"));


		responseStructure.setStatus(HttpStatus.FOUND.value());
		responseStructure.setMessage("School data found in database");
		responseStructure.setData(fetchedSchool);

		return new ResponseEntity<ResponseStructure<School>>(responseStructure, HttpStatus.FOUND);

	}

	@Override
	public ResponseEntity<ResponseStructure<List<School>>> findAllSchool() {

		List<School> all = schoolRepo.findAll();

		ResponseStructure<List<School>> rs = new ResponseStructure<List<School>>();
		rs.setStatus(HttpStatus.FOUND.value());
		rs.setMessage("School data found in database");
		rs.setData(all);

		return new ResponseEntity<ResponseStructure<List<School>>>(rs, HttpStatus.FOUND);

	}

}
