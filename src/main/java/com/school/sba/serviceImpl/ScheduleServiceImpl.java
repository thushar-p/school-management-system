package com.school.sba.serviceimpl;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.Schedule;
import com.school.sba.exception.ScheduleAlreadyPresentException;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.repository.ScheduleRepository;
import com.school.sba.repository.SchoolRepository;
import com.school.sba.requestdto.ScheduleRequest;
import com.school.sba.responsedto.ScheduleResponse;
import com.school.sba.service.ScheduleService;
import com.school.sba.util.ResponseStructure;

@Service
public class ScheduleServiceImpl implements ScheduleService{

	@Autowired
	private ScheduleRepository scheduleRepository;
	
	@Autowired
	private SchoolRepository schoolRepository;
	
	@Autowired
	private ResponseStructure<ScheduleResponse> structure;
	
	
	private ScheduleResponse mapToScheduleResponse(Schedule schedule) {
		
		
		long m = Duration.ofMinutes(schedule.getClassHoursLengthInMinutes().toMinutes()).toSeconds();
		
		return ScheduleResponse.builder()
				.scheduleId(schedule.getScheduleId())
				.opensAt(schedule.getOpensAt())
				.closesAt(schedule.getClosesAt())
				.classHoursPerDay(schedule.getClassHoursPerDay())
				.classHoursLengthInMinutes((int)
						(Duration.ofMinutes(schedule.getClassHoursLengthInMinutes().toMinutes())
								.toMinutes()))
				.breakTime(schedule.getBreakTime())
				.breakLengthInMinutes(((int)
						(Duration.ofMinutes(schedule.getBreakLengthInMinutes().toMinutes())
								.toMinutes())))
				.lunchLengthInMinutes((int)
						(Duration.ofMinutes(schedule.getLunchLengthInMinutes().toMinutes())
								.toMinutes()))
				.lunchTime(schedule.getLunchTime())
				.build();
	}

	private Schedule mapToSchedule(ScheduleRequest scheduleRequest) {
		return Schedule.builder()
				.opensAt(scheduleRequest.getOpensAt())
				.closesAt(scheduleRequest.getClosesAt())
				.classHoursPerDay(scheduleRequest.getClassHoursPerDay())
				.classHoursLengthInMinutes(Duration.ofMinutes(scheduleRequest.getClassHoursLengthInMinutes()))
				.breakTime(scheduleRequest.getBreakTime())
				.breakLengthInMinutes(Duration.ofMinutes(scheduleRequest.getBreakLengthInMinutes()))
				.lunchLengthInMinutes(Duration.ofMinutes(scheduleRequest.getLunchLengthInMinutes()))
				.lunchTime(scheduleRequest.getLunchTime())
				.build();
	}
	
	@Override
	public ResponseEntity<ResponseStructure<ScheduleResponse>> saveSchedule(int schoolId,
			ScheduleRequest scheduleRequest) {
		
		return schoolRepository.findById(schoolId)
		.map(school -> {
			if(school.getSchedule() == null) {
				Schedule schedule = scheduleRepository.save(mapToSchedule(scheduleRequest));
				
				school.setSchedule(schedule);
				
				schoolRepository.save(school);
				
				structure.setStatus(HttpStatus.CREATED.value());
				structure.setMessage("schedule added successfully");
				structure.setData(mapToScheduleResponse(schedule));
				
				return new ResponseEntity<ResponseStructure<ScheduleResponse>>(structure, HttpStatus.CREATED);
			}
			else {
				throw new ScheduleAlreadyPresentException("Schedule is already added");
			}
		})
		.orElseThrow(() -> new SchoolNotFoundByIdException("school not found"));
		
	}

	

}
