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

import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.service.UserService;
import com.school.sba.util.ResponseStructure;

import jakarta.validation.Valid;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/users/register")
	public ResponseEntity<ResponseStructure<UserResponse>> saveUser(@RequestBody @Valid UserRequest userRequest){
		return userService.saveUser(userRequest);
	}
	
	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(@PathVariable("userId") int userId){
		return userService.findUser(userId);
	}
	
	@DeleteMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> deleteUser(@PathVariable("userId") int userId){
		return userService.deleteUser(userId);
	}
	
	@PutMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(@PathVariable("userId") int userId,
			@RequestBody UserRequest userRequest){
		return userService.updateUser(userId, userRequest);
	}
	
}
