package com.school.sba.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.serviceimpl.AcademicProgramServiceImpl;
import com.school.sba.serviceimpl.ClassHourServiceImpl;
import com.school.sba.serviceimpl.SchoolServiceImpl;
import com.school.sba.serviceimpl.UserServiceImpl;

@Component
public class ScheduledJobs {
	
	@Autowired
	private UserServiceImpl userServiceImpl;
	
	@Autowired
	private AcademicProgramServiceImpl academicProgramServiceImpl;
	
	@Autowired
	private AcademicProgramRepository academicProgramRepository;
	
	@Autowired
	private ClassHourServiceImpl classHourServiceImpl;
	
	@Autowired
	private SchoolServiceImpl schoolServiceImpl;
	
	@Scheduled(fixedDelay = 2000L)
	public void hardDelete() {
		userServiceImpl.hardDeleteUser();
		academicProgramServiceImpl.hardDeleteAcademicProgram();
		schoolServiceImpl.hardDeleteSchool();
	}
	
	/*
	@Scheduled(cron = "0 0 0 * * MON")
	public void updateClassHour() {
		 System.out.println("hello 1");
		academicProgramRepository.findAll().forEach(academicProgram -> {
			classHourServiceImpl.classHourGen(academicProgram.getProgramId(), LocalDateTime.now());
		});
	}
	*/
	
	@Scheduled(cron = "0 0 15 * * MON")
    public void myTask() {
        // Your task logic here
        System.out.println("Task executed every Monday at midnight");
    }

}
