package com.school.sba.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.School;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.SchoolCannotBeCreatedException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.exception.UserNotFoundByIdException;
import com.school.sba.repository.SchoolRepository;
import com.school.sba.repository.UserRepository;
import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.responsedto.SchoolResponse;
import com.school.sba.service.SchoolService;
import com.school.sba.util.ResponseStructure;


@Service
public class SchoolServiceImpl implements SchoolService{

	@Autowired
	private SchoolRepository schoolRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ResponseStructure<SchoolResponse> responseStructure;


	private School mapToSchool(SchoolRequest schoolRequest) {
		return School.builder()
				.schoolName(schoolRequest.getSchoolName())
				.schoolEmailId(schoolRequest.getSchoolEmailId())
				.schoolContactNumber(schoolRequest.getSchoolContactNumber())
				.schoolAddress(schoolRequest.getSchoolAddress())
				.build();
	}

	private SchoolResponse mapToUserResponse(School school) {
		return SchoolResponse.builder()
				.schoolId(school.getSchoolId())
				.schoolName(school.getSchoolName())
				.schoolEmailId(school.getSchoolEmailId())
				.schoolContactNumber(school.getSchoolContactNumber())
				.schoolAddress(school.getSchoolAddress())
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> createSchool(int userId, SchoolRequest schoolRequest){

		return userRepo.findById(userId)
				.map(user -> {
					if(user.getUserRole().equals(UserRole.ADMIN)) {
						if(user.getSchool() == null) {
							School school = schoolRepo.save(mapToSchool(schoolRequest));

							user.setSchool(school);

							userRepo.save(user);

							responseStructure.setStatus(HttpStatus.CREATED.value());
							responseStructure.setMessage("School inserted successfully");
							responseStructure.setData(mapToUserResponse(school));

							return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.CREATED);
						}
						else {
							throw new SchoolCannotBeCreatedException("school is already present");
						}
					}
					else {
						throw new SchoolCannotBeCreatedException("school can be created only by ADMIN");
					}
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

	}



	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> deleteSchool(Integer schoolId) {

		School existingSchool = schoolRepo.findById(schoolId)
				.orElseThrow(() -> new SchoolNotFoundByIdException("school object cannot be deleted due to absence of school id"));

		schoolRepo.deleteById(schoolId);

		responseStructure.setStatus(HttpStatus.OK.value());
		responseStructure.setMessage("School data deleted successfully from database");
		responseStructure.setData(mapToUserResponse(existingSchool));

		return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.OK);
	}



	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> updateSchool(Integer schoolId, SchoolRequest schoolRequest)
			throws SchoolNotFoundByIdException {

		School existingSchool = schoolRepo.findById(schoolId)
				.map(u -> {
					School school = mapToSchool(schoolRequest);
					school.setSchoolId(schoolId);
					return schoolRepo.save(school);
				})
				.orElseThrow(() -> new SchoolNotFoundByIdException("school object cannot be updated due to absence of technical problems"));


		responseStructure.setStatus(HttpStatus.OK.value());
		responseStructure.setMessage("School data updated successfully in database");
		responseStructure.setData(mapToUserResponse(existingSchool));

		return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.OK);

	}



	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> findSchool(Integer schoolId)
			throws SchoolNotFoundByIdException {

		School fetchedSchool = schoolRepo.findById(schoolId)
				.orElseThrow(() -> new SchoolNotFoundByIdException("School object cannot be fetched because it is not present in DB"));


		responseStructure.setStatus(HttpStatus.FOUND.value());
		responseStructure.setMessage("School data found in database");
		responseStructure.setData(mapToUserResponse(fetchedSchool));

		return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.FOUND);

	}

	//	@Override
	//	public ResponseEntity<ResponseStructure<List<SchoolResponse>>> findAllSchool() {
	//
	//		List<School> all = schoolRepo.findAll();
	//
	//		ResponseStructure<List<SchoolResponse>> rs = new ResponseStructure<List<SchoolResponse>>();
	//		rs.setStatus(HttpStatus.FOUND.value());
	//		rs.setMessage("School data found in database");
	//		rs.setData(all);
	//
	//		return new ResponseEntity<ResponseStructure<List<SchoolResponse>>>(rs, HttpStatus.FOUND);
	//
	//	}

}
