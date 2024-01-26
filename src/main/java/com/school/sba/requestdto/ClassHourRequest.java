package com.school.sba.requestdto;

import java.time.LocalTime;

import com.school.sba.enums.ClassStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassHourRequest {
	
	private LocalTime classBeginsAt;
	private LocalTime classEndsAt;
	private int classRoomNumber;
	private ClassStatus classStatus;

}
