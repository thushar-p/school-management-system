package com.school.sba.requestdto;

import java.time.Duration;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ScheduleRequest {
	private LocalTime opensAt;
	private LocalTime closesAt;
	private int classHoursPerDay;
	private int classHoursLengthInMinutes;
	private LocalTime breakTime;
	private int breakLengthInMinutes;
	private LocalTime lunchTime;
	private int lunchLengthInMinutes;
}
