package com.school.sba.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.util.ResponseStructure;

public interface UserService {

	ResponseEntity<ResponseStructure<UserResponse>> saveUser(UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> findUser(Integer userId);

	ResponseEntity<ResponseStructure<UserResponse>> deleteUser(int userId);

	ResponseEntity<ResponseStructure<UserResponse>> updateUser(int userId, UserRequest userRequest);

	ResponseEntity<ResponseStructure<List<UserResponse>>> getTeacher();

}
