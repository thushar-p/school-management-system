package com.school.sba.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.AdminAlreadyExistException;
import com.school.sba.exception.UserNotFoundIdException;
import com.school.sba.repository.UserRespository;
import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.service.UserService;
import com.school.sba.util.ResponseStructure;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRespository userRespository;

	@Autowired
	private ResponseStructure<UserResponse> structure;

	private User mapToUser(UserRequest userRequest) {
		return User.builder().userName(userRequest.getUserName()).userPassword(userRequest.getUserPassword())
				.userFirstName(userRequest.getUserFirstName()).userLastName(userRequest.getUserLastName())
				.userEmail(userRequest.getUserEmail()).userContact(userRequest.getUserContact())
				.userRole(userRequest.getUserRole()).build();
	}

	private UserResponse mapToUserResponse(User user) {
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.userFirstName(user.getUserFirstName())
				.userLastName(user.getUserLastName())
				.userEmail(user.getUserEmail())
				.userContact(user.getUserContact())
				.userRole(user.getUserRole()).build();
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> saveUser(UserRequest userRequest) {

		if (userRequest.getUserRole().equals(UserRole.ADMIN)) {
			if (userRespository.existsByUserRole(userRequest.getUserRole()) == false) {
				User user = userRespository.save(mapToUser(userRequest));

				structure.setStatus(HttpStatus.CREATED.value());
				structure.setMessage("user saved successfully");
				structure.setData(mapToUserResponse(user));

				return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
			} else {
				throw new AdminAlreadyExistException("Admin already exist");
			}
		} else {
			User user = userRespository.save(mapToUser(userRequest));

			structure.setStatus(HttpStatus.CREATED.value());
			structure.setMessage("user saved successfully");
			structure.setData(mapToUserResponse(user));

			return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);

		}

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(Integer userId) {

		User user = userRespository.findById(userId)
				.orElseThrow(() -> new UserNotFoundIdException("user not found"));

		structure.setStatus(HttpStatus.FOUND.value());
		structure.setMessage("user found successfully");
		structure.setData(mapToUserResponse(user));

		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.FOUND);
	}

	
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> deleteUser(int userId) {
		User user = userRespository.findById(userId)
				.orElseThrow(() -> new UserNotFoundIdException("user not found"));
		
		user.setDeleted(true);
		user = userRespository.save(user);
		
		structure.setStatus(HttpStatus.OK.value());
		structure.setMessage("user deleted successfully");
		structure.setData(mapToUserResponse(user));

		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.FOUND);
	}
	

}
