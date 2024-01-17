package com.school.sba.requestdto;

import java.time.LocalTime;

import com.school.sba.enums.ProgramType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademicProgramRequest {

	private ProgramType programType;
	private String programName;
	private LocalTime programBeginsAt;
	private LocalTime programEndsAt;
	
}
