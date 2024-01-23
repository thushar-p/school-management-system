package com.school.sba.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	boolean existsByUserRole(UserRole userRole);

	boolean existsByIsDeletedAndUserRole(boolean b, UserRole userRole);

	Optional<User> findByUserName(String username);
	
//	boolean findByIsDeletedAndUserRole(boolean b, UserRole userRole);
	
//	List<UserRole> findAllByUserRole(UserRole userRole);

}
