package com.school.sba.serviceimpl;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.school.sba.entity.School;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.InvalidWeekDayException;
import com.school.sba.exception.SchoolCannotBeCreatedException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.exception.UserNotFoundByIdException;
import com.school.sba.repository.SchoolRepository;
import com.school.sba.repository.UserRepository;
import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.responsedto.SchoolResponse;
import com.school.sba.service.SchoolService;
import com.school.sba.util.ResponseEntityProxy;
import com.school.sba.util.ResponseStructure;


@Service
public class SchoolServiceImpl implements SchoolService{

	@Autowired
	private SchoolRepository schoolRepo;

	@Autowired
	private UserRepository userRepo;


	private School mapToSchool(SchoolRequest schoolRequest) {
		return School.builder()
				.schoolName(schoolRequest.getSchoolName())
				.schoolEmailId(schoolRequest.getSchoolEmailId())
				.schoolContactNumber(schoolRequest.getSchoolContactNumber())
				.schoolAddress(schoolRequest.getSchoolAddress())
				.weekOffDay(DayOfWeek.valueOf(schoolRequest.getWeekOffDay().toUpperCase()))
				.build();
	}

	private SchoolResponse mapToUserResponse(School school) {
		return SchoolResponse.builder()
				.schoolId(school.getSchoolId())
				.schoolName(school.getSchoolName())
				.schoolEmailId(school.getSchoolEmailId())
				.schoolContactNumber(school.getSchoolContactNumber())
				.schoolAddress(school.getSchoolAddress())
				.weekOfDay(school.getWeekOffDay())
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> createSchool(SchoolRequest schoolRequest){

		String username = SecurityContextHolder.getContext()
				.getAuthentication()
				.getName();
		
		return userRepo.findByUserName(username)
				.map(user -> {
					
					DayOfWeek weekOffDay = DayOfWeek.valueOf(schoolRequest.getWeekOffDay().toUpperCase());
					if(!EnumSet.allOf(DayOfWeek.class).contains(weekOffDay))
						throw new InvalidWeekDayException("invalid week day");
					
					
					if(user.getUserRole().equals(UserRole.ADMIN)) {
						if(user.getSchool() == null) {
							School school = schoolRepo.save(mapToSchool(schoolRequest));
							
							userRepo.findAll().forEach(userFromRepo -> {
								userFromRepo.setSchool(school);
								userRepo.save(user);
							});

							return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
									"School inserted successfully",
									mapToUserResponse(school));
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
	public ResponseEntity<ResponseStructure<SchoolResponse>> updateSchool(Integer schoolId, SchoolRequest schoolRequest)
			throws SchoolNotFoundByIdException {

		return schoolRepo.findById(schoolId)
				.map( school -> {
					
					DayOfWeek weekOffDay = DayOfWeek.valueOf(schoolRequest.getWeekOffDay().toUpperCase());
					if(!EnumSet.allOf(DayOfWeek.class).contains(weekOffDay))
						throw new InvalidWeekDayException("invalid week day");
					
					school = mapToSchool(schoolRequest);
					school.setSchoolId(schoolId);
					school = schoolRepo.save(school);
					
					return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
							"School updated successfully",
							mapToUserResponse(school));					
				})
				.orElseThrow(() -> new SchoolNotFoundByIdException("school object cannot be updated due to absence of technical problems"));

	}
	
	/*

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
	public ResponseEntity<ResponseStructure<SchoolResponse>> findSchool(Integer schoolId)
			throws SchoolNotFoundByIdException {

		School fetchedSchool = schoolRepo.findById(schoolId)
				.orElseThrow(() -> new SchoolNotFoundByIdException("School object cannot be fetched because it is not present in DB"));


		responseStructure.setStatus(HttpStatus.FOUND.value());
		responseStructure.setMessage("School data found in database");
		responseStructure.setData(mapToUserResponse(fetchedSchool));

		return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.FOUND);

	}
	
	*/


}
