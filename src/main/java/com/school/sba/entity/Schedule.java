package com.school.sba.entity;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer scheduleId;
	private LocalTime opensAt;
	private LocalTime closesAt;
	private Integer classHoursPerDay;
	private LocalTime classHoursLength;
	private LocalTime breakTime;
	private LocalTime beakLength;
	private LocalTime lunchTime;
	private LocalTime lunchLength;
	
}
