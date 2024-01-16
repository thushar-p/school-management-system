package com.school.sba.entity;

import com.school.sba.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	
//	@Column(unique = true)
	private String userName;
	private String userPassword;
	private String userFirstName;
	private String userLastName;
	
//	@Column(unique = true)
	private Long userContact;
	
//	@Column(unique = true)
	private String userEmail;
	
	private UserRole userRole;
}
