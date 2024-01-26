package com.school.sba.responsedto;

import java.time.LocalTime;

import com.school.sba.enums.ClassStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassHourResponse {

	private int classHourId;
	private LocalTime classBeginsAt;
	private LocalTime classEndsAt;
	private int classRoomNumber;
	private ClassStatus classStatus;
	
}
