package com.school.sba.serviceimpl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.AcademicProgram;
import com.school.sba.entity.ClassHour;
import com.school.sba.entity.Schedule;
import com.school.sba.entity.School;
import com.school.sba.enums.ClassStatus;
import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.exception.ScheduleNotFoundException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.ClassHourRepository;
import com.school.sba.responsedto.ClassHourResponse;
import com.school.sba.service.ClassHourService;
import com.school.sba.util.ResponseEntityProxy;
import com.school.sba.util.ResponseStructure;

@Service
public class ClassHourServiceImpl implements ClassHourService {

	@Autowired
	private ClassHourRepository classHourRepository;

	@Autowired
	private AcademicProgramRepository academicProgramRepository;

	private List<ClassHourResponse> mapToClassHourResponse(List<ClassHour> savedList) {

		List<ClassHourResponse> listOfClassHourResponses = new ArrayList<>();
		savedList.forEach(classHour -> {
			listOfClassHourResponses
					.add(ClassHourResponse.builder()
							.classBeginsAt(classHour.getClassBeginsAt().toLocalTime())
							.classEndsAt(classHour.getClassEndsAt().toLocalTime())
							.classRoomNumber(classHour.getClassRoomNumber())
							.classStatus(classHour.getClassStatus())
							.day(classHour.getClassBeginsAt().getDayOfWeek())
							.date(classHour.getClassBeginsAt().toLocalDate())
							.build());
		});

		return listOfClassHourResponses;

	}

	private boolean isBreakTime(LocalDateTime currentTime, Schedule schedule) {
		LocalTime breakTimeStart = schedule.getBreakTime();
		LocalTime breakTimeEnd = breakTimeStart.plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());

		return (currentTime.toLocalTime().isAfter(breakTimeStart) && currentTime.toLocalTime().isBefore(breakTimeEnd));

	}

	private boolean isLunchTime(LocalDateTime currentTime, Schedule schedule) {
		LocalTime lunchTimeStart = schedule.getLunchTime();
		LocalTime lunchTimeEnd = lunchTimeStart.plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());

		return (currentTime.toLocalTime().isAfter(lunchTimeStart) && currentTime.toLocalTime().isBefore(lunchTimeEnd));

	}

	public List<ClassHour> generateClassHour(AcademicProgram academicProgram) {

		List<ClassHour> listOfClassHour = new ArrayList<ClassHour>();

		School school = academicProgram.getSchool();
		Schedule schedule = school.getSchedule();

		if (schedule != null) {

			int weekOffDay = school.getWeekOffDay().getValue();
			int startingDayOfWeek;
			
			if(weekOffDay == 7) 
				startingDayOfWeek = 1;
			else
				startingDayOfWeek = weekOffDay+1;

			int classHoursPerDay = schedule.getClassHoursPerDay();
			int classHourLengthInMinutes = (int) schedule.getClassHoursLengthInMinutes().toMinutes();

			int currentDayNum = LocalDateTime.now().toLocalDate().getDayOfWeek().getValue();
			int diff = currentDayNum - startingDayOfWeek;
			
			LocalDateTime currentTime = LocalDateTime.now().minusDays(diff).with(schedule.getOpensAt());

			LocalTime breakTimeEnd = schedule.getBreakTime().plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());

			LocalTime lunchTimeEnd = schedule.getLunchTime().plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());

			for (int day = 1; day <= 6; day++) {

				for (int hour = 0; hour < classHoursPerDay + 2; hour++) {

					ClassHour classHour = new ClassHour();

					if (!currentTime.toLocalTime().equals(schedule.getLunchTime()) && !isLunchTime(currentTime, schedule)) {

						if (!currentTime.toLocalTime().equals(schedule.getBreakTime()) && !isBreakTime(currentTime, schedule)) {

							LocalDateTime beginsAt = currentTime;
							LocalDateTime endsAt = beginsAt.plusMinutes(classHourLengthInMinutes);

							classHour.setClassBeginsAt(beginsAt);
							classHour.setClassEndsAt(endsAt);
							classHour.setClassStatus(ClassStatus.NOT_SCHEDULED);

							currentTime = endsAt;

						} else {

							classHour.setClassBeginsAt(currentTime);
							classHour.setClassEndsAt(LocalDateTime.now().with(breakTimeEnd));

							classHour.setClassStatus(ClassStatus.BREAK_TIME);
							currentTime = currentTime.plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());
						}

					} else {

						classHour.setClassBeginsAt(currentTime);
						classHour.setClassEndsAt(LocalDateTime.now().with(lunchTimeEnd));
						classHour.setClassStatus(ClassStatus.LUNCH_TIME);
						currentTime = currentTime.plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());

					}

					classHour.setAcademicPrograms(academicProgram);
					listOfClassHour.add(classHour);
				}

				currentTime = currentTime.plusDays(1).with(schedule.getOpensAt());

			}
		} else {

			throw new ScheduleNotFoundException("schedule not found");

		}

		return listOfClassHour;

	}

	@Override
	public ResponseEntity<ResponseStructure<List<ClassHourResponse>>> addClassHour(int programId) {
		return academicProgramRepository.findById(programId)
				.map(academicProgram -> {

			List<ClassHour> listOfClassHours = generateClassHour(academicProgram);

			listOfClassHours.forEach(cl -> {
				System.out.println(cl);
			});

			List<ClassHour> savedList = classHourRepository.saveAll(listOfClassHours);
			
			return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED,
					"class hour generated successfully",
					mapToClassHourResponse(savedList));
			
		}).orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
	}

}
