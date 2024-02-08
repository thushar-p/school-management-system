package com.school.sba.serviceimpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.school.sba.entity.School;
import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.exception.AdminAlreadyFoundException;
import com.school.sba.exception.AdminCannotBeAssignedToAcademicProgram;
import com.school.sba.exception.AdminCannotBeDeletedException;
import com.school.sba.exception.AdminNotFoundException;
import com.school.sba.exception.InvalidUserRoleException;
import com.school.sba.exception.NoAssociatedObjectsFoundException;
import com.school.sba.exception.OnlyTeacherCanBeAssignedToSubjectException;
import com.school.sba.exception.SubjectNotFoundException;
import com.school.sba.exception.UserNotFoundByIdException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.ClassHourRepository;
import com.school.sba.repository.SubjectRepository;
import com.school.sba.repository.UserRepository;
import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.service.UserService;
import com.school.sba.util.ResponseEntityProxy;
import com.school.sba.util.ResponseStructure;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AcademicProgramRepository academicProgramRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ClassHourRepository classHourRepository;

	@Transactional
	public void hardDeleteUser() {

		userRepository.findByIsDeleted(true).forEach(user -> {

			user.getListOfAcademicPrograms().forEach(ap -> {
				ap.setListOfUsers(null);
			});

			classHourRepository.findByUser(user).forEach(classHour -> {
				classHour.setUser(null);
			});

			userRepository.delete(user);

		});

	}

	private User mapToUser(UserRequest userRequest) {
		return User.builder().userName(userRequest.getUserName())
				.userPassword(passwordEncoder.encode(userRequest.getUserPassword()))
				.userFirstName(userRequest.getUserFirstName())
				.userLastName(userRequest.getUserLastName())
				.userEmail(userRequest.getUserEmail())
				.userContact(Long.parseLong(userRequest.getUserContact()))
				.userRole(UserRole.valueOf(userRequest.getUserRole().toUpperCase()))
				.build();
	}

	private UserResponse mapToUserResponse(User user) {

		List<String> listOfProgramName = new ArrayList<>();

		if (user.getListOfAcademicPrograms() != null) {
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
				.subject(user.getSubject())
				.listOfAcademicPrograms(listOfProgramName)
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerAdmin(UserRequest userRequest) {
		
		try {
			if (!EnumSet.allOf(UserRole.class)
					.contains(UserRole.valueOf(userRequest.getUserRole().toUpperCase()))) {
			}
		} catch (Exception e) {
			throw new InvalidUserRoleException("invalid user role");
		}

		UserRole userRole = UserRole.valueOf(userRequest.getUserRole().toUpperCase());

		if (userRole.equals(UserRole.ADMIN)) {

			if (userRepository.existsByIsDeletedAndUserRole(false, userRole)) {
				throw new AdminAlreadyFoundException("Admin already exist");
			} else {
				if (userRepository.existsByIsDeletedAndUserRole(true, userRole)) {
					User user = userRepository.save(mapToUser(userRequest));

					return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
							"admin saved successfully",
							mapToUserResponse(user));
				} else {
					User user = userRepository.save(mapToUser(userRequest));
					return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
							"admin saved successfully",
							mapToUserResponse(user));

				}
			}
		} else {
			throw new AdminNotFoundException("admin not found");
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> addOtherUser(UserRequest userRequest) {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		try {
			if (!EnumSet.allOf(UserRole.class)
					.contains(UserRole.valueOf(userRequest.getUserRole().toUpperCase()))) {
			}
		} catch (Exception e) {
			throw new InvalidUserRoleException("invalid user role");
		}

		if (UserRole.valueOf(userRequest.getUserRole().toUpperCase()).equals(UserRole.ADMIN)) {
			throw new AdminAlreadyFoundException("admin already found");
		} else {
			return userRepository.findByUserName(username).map(admin -> {
				School school = admin.getSchool();

				User user = userRepository.save(mapToUser(userRequest));
				user.setSchool(school);
				user = userRepository.save(user);

				return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
						user.getUserRole().name().toLowerCase() + " saved successfully",
						mapToUserResponse(user));

			})
					.orElseThrow(() -> new AdminNotFoundException("admin not found"));
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(Integer userId) {

		return userRepository.findById(userId)
				.map(user -> {
					return ResponseEntityProxy.setResponseStructure(HttpStatus.FOUND,
							"user found successfully",
							mapToUserResponse(user));

				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> softDeleteUser(int userId) {

		return userRepository.findById(userId)
				.map(user -> {
					if (user.isDeleted()) {
						throw new UserNotFoundByIdException("User already deleted");
					}

					if (user.getUserRole().equals(UserRole.ADMIN)) {
						throw new AdminCannotBeDeletedException("admin cannot be deleted");
					}

					user.setDeleted(true);
					userRepository.save(user);

					return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
							"user deleted successfully",
							mapToUserResponse(user));
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(int userId, UserRequest userRequest) {

		try {
			if (!EnumSet.allOf(UserRole.class)
					.contains(UserRole.valueOf(userRequest.getUserRole().toUpperCase()))) {
			}
		} catch (Exception e) {
			throw new InvalidUserRoleException("invalid user role");
		}

		return userRepository.findById(userId)
				.map(user -> {					
					User mappedUser = mapToUser(userRequest);
					mappedUser.setUserId(userId);
					user = userRepository.save(mappedUser);

					return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
							"user updated successfully",
							mapToUserResponse(user));
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> assignToAcademicProgram(int programId, int userId) {

		return userRepository.findById(userId)
				.map(user -> {
					if (user.getUserRole().equals(UserRole.ADMIN)) {
						throw new AdminCannotBeAssignedToAcademicProgram("admin cannot be assigned");
					} else {
						return academicProgramRepository.findById(programId)
								.map(academicProgram -> {

									if (user.getUserRole().equals(UserRole.TEACHER)) {

										if (academicProgram.getListOfSubject().contains(user.getSubject())) {

											academicProgram.getListOfUsers().add(user);
											user.getListOfAcademicPrograms().add(academicProgram);

											userRepository.save(user);
											academicProgramRepository.save(academicProgram);

											return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
													user.getUserRole().name().toLowerCase()
															+ " assigned to academic program successfully",
													mapToUserResponse(user));

										} else {
											throw new SubjectNotFoundException("subject not found");
										}
									} else {
										academicProgram.getListOfUsers().add(user);
										user.getListOfAcademicPrograms().add(academicProgram);

										userRepository.save(user);
										academicProgramRepository.save(academicProgram);

										return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
												user.getUserRole().name().toLowerCase()
														+ " assigned to academic program successfully",
												mapToUserResponse(user));
									}
								})
								.orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
					}
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> assignSubjectToTeacher(int subjectId, int userId) {

		return userRepository.findById(userId)
				.map(user -> {
					if (user.getUserRole().equals(UserRole.TEACHER)) {
						return subjectRepository.findById(subjectId)
								.map(subject -> {
									user.setSubject(subject);
									userRepository.save(user);

									return ResponseEntityProxy.setResponseStructure(HttpStatus.OK,
											"subject assigned to teacher successfully",
											mapToUserResponse(user));

								})
								.orElseThrow(() -> new SubjectNotFoundException("subject not found"));
					} else {
						throw new OnlyTeacherCanBeAssignedToSubjectException(
								"only teacher can be assigned to the subject");
					}
				})
				.orElseThrow(() -> new UserNotFoundByIdException("user not found"));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<UserResponse>>> findAllByRole(int programId, String userRole) {

		try {
			if (!EnumSet.allOf(UserRole.class)
					.contains(UserRole.valueOf(userRole.toUpperCase()))) {
			}
		} catch (Exception e) {
			throw new InvalidUserRoleException("invalid user role");
		}

		return academicProgramRepository.findById(programId)
				.map(academicProgram -> {

					if (UserRole.valueOf(userRole.toUpperCase()).equals(UserRole.ADMIN))
						throw new IllegalArgumentException("admin cannot be fetched");

					List<UserResponse> collect = userRepository
							.findByUserRoleAndListOfAcademicPrograms(UserRole.valueOf(userRole.toUpperCase()),
									academicProgram)
							.stream()
							.map(this::mapToUserResponse)
							.collect(Collectors.toList());

					if (collect.isEmpty()) {
						throw new NoAssociatedObjectsFoundException(
								"academic program not found with " + programId + " and userrole with " + userRole);
					} else {
						return ResponseEntityProxy.setResponseStructure(HttpStatus.FOUND,
								"list of " + userRole + " found successfully",
								collect);
					}
				})
				.orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
	}

}
