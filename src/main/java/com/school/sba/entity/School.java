	package com.school.sba.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Builder
public class School {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer schoolId;
	private String schoolName;
	private Long schoolContactNumber;
	private String schoolEmailId;
	private String schoolAddress;
	
	@OneToOne
	private Schedule schedule;
	
}
