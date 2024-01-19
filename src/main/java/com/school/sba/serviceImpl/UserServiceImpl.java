package com.school.sba.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.exception.AdminCannotBeAssignedToAcademicProgram;
import com.school.sba.exception.AdminNotFoundException;
import com.school.sba.exception.SchoolCannotBeCreatedException;
import com.school.sba.exception.UserNotFoundByIdException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.UserRepository;
import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.service.UserService;
import com.school.sba.util.ResponseStructure;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AcademicProgramRepository academicProgramRepository;
	
	@Autowired
	private ResponseStructure<UserResponse> structure;
	
	private User mapToUser(UserRequest userRequest) {
		return User.builder().userName(userRequest.getUserName())
				.userPassword(userRequest.getUserPassword())
				.userFirstName(userRequest.getUserFirstName())
				.userLastName(userRequest.getUserLastName())
				.userEmail(userRequest.getUserEmail())
				.userContact(userRequest.getUserContact())
				.userRole(userRequest.getUserRole())
				.school(userRequest.getSchool())
				.build();
	}

	
	private UserResponse mapToUserResponse(User user) {
		List<String> listOfProgramName = new ArrayList<>();
		
		if( user.getListOfAcademicPrograms().isEmpty()) {
			
		}
		else {
			user.getListOfAcademicPrograms().forEach(academicProgram -> {
				listOfProgramName.add(academicProgram.getProgramName());
			});
		}
		
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.userFirstName(user.getUserFirstName())
				.userLastName(user.getUserLastName())
				.userEmail(user.getUserEmail())
				.userContact(user.getUserContact())
				.userRole(user.getUserRole())
				.listOfAcademicPrograms(listOfProgramName)
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> saveUser(UserRequest userRequest) {

		if (userRequest.getUserRole().equals(UserRole.ADMIN)) {
			if (userRepository.existsByIsDeletedAndUserRole(false , userRequest.getUserRole()))  {
				throw new SchoolCannotBeCreatedException("Admin already exist");
			} 
			else {
				if(userRepository.existsByIsDeletedAndUserRole(true, userRequest.getUserRole())) {
					User user = userRepository.save(mapToUser(userRequest));

					structure.setStatus(HttpStatus.CREATED.value());
					structure.setMessage("user saved successfully");
					structure.setData(mapToUserResponse(user));

					return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
				}
				else {
					User user = userRepository.save(mapToUser(userRequest));

					structure.setStatus(HttpStatus.CREATED.value());
					structure.setMessage("user saved successfully");
					structure.setData(mapToUserResponse(user));

					return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);	
				}
			}
		}
		else {
			
			if(userRepository.existsByUserRole(UserRole.ADMIN)) {
				User user = userRepository.save(mapToUser(userRequest));
				
				structure.setStatus(HttpStatus.CREATED.value());
				structure.setMessage("user saved successfully");
				structure.setData(mapToUserResponse(user));

				return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
			}
			else {
				throw new AdminNotFoundException("admin not found");
			}
		}

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(Integer userId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

		structure.setStatus(HttpStatus.FOUND.value());
		structure.setMessage("user found successfully");
		structure.setData(mapToUserResponse(user));

		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.FOUND);
	}



	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> deleteUser(int userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

		if(user.isDeleted()) {
			throw new UserNotFoundByIdException("User already deleted");
		}

		user.setDeleted(true);
		userRepository.save(user);

		structure.setStatus(HttpStatus.OK.value());
		structure.setMessage("user deleted successfully");
		structure.setData(mapToUserResponse(user));

		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(int userId, UserRequest userRequest) {

		User user = userRepository.findById(userId)
				.map( u -> {
					User mappedUser = mapToUser(userRequest);
					mappedUser.setUserId(userId);
					return userRepository.save(mappedUser);
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

		structure.setStatus(HttpStatus.OK.value());
		structure.setMessage("user updated successfully");
		structure.setData(mapToUserResponse(user));

		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> assignTeacherAndStudent(int programId, int userId) {
		
		return userRepository.findById(userId)
		.map(user -> {
			if(user.getUserRole().equals(UserRole.ADMIN)) {
				throw new AdminCannotBeAssignedToAcademicProgram("admin cannot be assigned");
			}
			else {
				return academicProgramRepository.findById(programId)
				.map(academicProgram -> {
					academicProgram.getListOfUsers().add(user);
					user.getListOfAcademicPrograms().add(academicProgram);
					
					userRepository.save(user);
					academicProgramRepository.save(academicProgram);
					
					structure.setStatus(HttpStatus.OK.value());
					structure.setMessage("user updated successfully");
					structure.setData(mapToUserResponse(user));

					return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.OK);
					
				})
				.orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
			}
		})
		.orElseThrow(() -> new UserNotFoundByIdException("user not found"));
		
	}


}
