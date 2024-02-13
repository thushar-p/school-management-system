package com.school.sba.serviceimpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
import com.school.sba.entity.Subject;
import com.school.sba.entity.User;
import com.school.sba.enums.ClassStatus;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.AcademicProgramNotAssignedException;
import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.exception.ClassCannotAssignedException;
import com.school.sba.exception.ClassHourAlreadyGeneratedException;
import com.school.sba.exception.ClassHourNotFoundException;
import com.school.sba.exception.IdNotFoundException;
import com.school.sba.exception.PreviousClassHourNotFoundException;
import com.school.sba.exception.RoomAlreadyAssignedException;
import com.school.sba.exception.ScheduleNotFoundException;
import com.school.sba.exception.SubjectNotAssignedToTeacherException;
import com.school.sba.exception.SubjectNotFoundException;
import com.school.sba.exception.TeacherNotFoundByIdException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.ClassHourRepository;
import com.school.sba.repository.SubjectRepository;
import com.school.sba.repository.UserRepository;
import com.school.sba.requestdto.ClassHourRequest;
import com.school.sba.responsedto.ClassHourResponse;
import com.school.sba.service.ClassHourService;
import com.school.sba.util.ResponseEntityProxy;
import com.school.sba.util.ResponseStructure;

@Service
public class ClassHourServiceImpl implements ClassHourService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ClassHourRepository classHourRepository;

	@Autowired
	private AcademicProgramRepository academicProgramRepository;

	private List<ClassHourResponse> mapToClassHourResponse(List<ClassHour> savedList) {

		List<ClassHourResponse> listOfClassHourResponses = new ArrayList<>();
		savedList.forEach(classHour -> {
			listOfClassHourResponses
			.add(ClassHourResponse.builder().classBeginsAt(classHour.getClassBeginsAt().toLocalTime())
					.classEndsAt(classHour.getClassEndsAt().toLocalTime())
					.classRoomNumber(classHour.getClassRoomNumber()).classStatus(classHour.getClassStatus())
					.day(classHour.getClassBeginsAt().getDayOfWeek())
					.date(classHour.getClassBeginsAt().toLocalDate()).build());
		});

		return listOfClassHourResponses;

	}

	private boolean isRoomReserved(LocalDateTime classBeginsAt, LocalDateTime classEndsAt, int classRoomNumber) {
		return classHourRepository.existsByClassBeginsAtAndClassRoomNumber(classBeginsAt, classRoomNumber);
	}

	private boolean isBreakTime(LocalDateTime currentTime, LocalTime breakStartTime, LocalTime breakEndTime) {
		return currentTime.toLocalTime().equals(breakStartTime) || (currentTime.toLocalTime().isAfter(breakStartTime)
				&& currentTime.toLocalTime().isBefore(breakEndTime));

	}

	private boolean isLunchTime(LocalDateTime currentTime, LocalTime lunchStartTime, LocalTime lunchEndTime) {
		return currentTime.toLocalTime().equals(lunchStartTime) || (currentTime.toLocalTime().isAfter(lunchStartTime)
				&& currentTime.toLocalTime().isBefore(lunchEndTime));

	}

	public List<ClassHour> generateClassHour(AcademicProgram academicProgram) {

		List<ClassHour> listOfClassHour = new ArrayList<ClassHour>();

		School school = academicProgram.getSchool();
		Schedule schedule = school.getSchedule();

		if (schedule != null) {

			if (academicProgram.getListOfClassHours().isEmpty()) {

				LocalDate programBeginsAt = academicProgram.getProgramBeginsAt();

				LocalDateTime currentDateTime;
				if (programBeginsAt.isAfter(LocalDate.now()))
					currentDateTime = programBeginsAt.atTime(schedule.getOpensAt());
				else
					currentDateTime = LocalDateTime.now().with(schedule.getOpensAt());

				LocalDateTime lastWorkingDay;
				if (!currentDateTime.equals(currentDateTime.with(DayOfWeek.MONDAY)))
					lastWorkingDay = LocalDateTime.now().plusWeeks(1).with(DayOfWeek.SATURDAY);
				else
					lastWorkingDay = LocalDateTime.now().with(DayOfWeek.SATURDAY);

				LocalTime breakTimeStart = schedule.getBreakTime();
				LocalTime breakTimeEnd = schedule.getBreakTime()
						.plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());
				LocalTime lunchTimeStart = schedule.getLunchTime();
				LocalTime lunchTimeEnd = schedule.getLunchTime()
						.plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());

				while (currentDateTime.isBefore(lastWorkingDay.plusDays(1))) {

					if (!currentDateTime.equals(currentDateTime.with(DayOfWeek.SUNDAY))) {

						for (int hour = 0; hour < schedule.getClassHoursPerDay() + 2; hour++) {

							ClassHour classHour = new ClassHour();

							if (isLunchTime(currentDateTime, lunchTimeStart, lunchTimeEnd)) {

								classHour.setClassBeginsAt(currentDateTime);
								classHour.setClassEndsAt(LocalDateTime.now().with(lunchTimeEnd));
								classHour.setClassStatus(ClassStatus.LUNCH_TIME);
								currentDateTime = currentDateTime
										.plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());

							} else {
								if (isBreakTime(currentDateTime, breakTimeStart, breakTimeEnd)) {

									classHour.setClassBeginsAt(currentDateTime);
									classHour.setClassEndsAt(LocalDateTime.now().with(breakTimeEnd));

									classHour.setClassStatus(ClassStatus.BREAK_TIME);
									currentDateTime = currentDateTime
											.plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());

								} else {

									LocalDateTime beginsAt = currentDateTime;
									LocalDateTime endsAt = beginsAt
											.plusMinutes(schedule.getClassHoursLengthInMinutes().toMinutes());

									classHour.setClassBeginsAt(beginsAt);
									classHour.setClassEndsAt(endsAt);
									classHour.setClassStatus(ClassStatus.NOT_SCHEDULED);

									currentDateTime = endsAt;
								}
							}
							classHour.setAcademicPrograms(academicProgram);
							listOfClassHour.add(classHour);
						}
						currentDateTime = currentDateTime.plusDays(1).with(schedule.getOpensAt());
					} else
						currentDateTime = currentDateTime.plusDays(1).with(schedule.getOpensAt());
					;
				}
			} else
				throw new ClassHourAlreadyGeneratedException("class hour already generated");
		} else
			throw new ScheduleNotFoundException("schedule not found");

		return listOfClassHour;

	}

	@Override
	public ResponseEntity<ResponseStructure<List<ClassHourResponse>>> addClassHour(int programId) {
		return academicProgramRepository.findById(programId).map(academicProgram -> {
			List<ClassHour> listOfClassHours = academicProgram.getListOfClassHours();
			
			if(listOfClassHours.isEmpty())
				listOfClassHours = generateClassHour(academicProgram);
			else {
				for(ClassHour cl : listOfClassHours) {
					if(cl.getClassBeginsAt().toLocalDate().isEqual(LocalDate.now())) {
						listOfClassHours = generateClassHour(academicProgram);
					}
				}
			}

			listOfClassHours = classHourRepository.saveAll(listOfClassHours);

			return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED, "class hour generated successfully",
					mapToClassHourResponse(listOfClassHours));

		}).orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<ClassHourResponse>>> updateClassHour(
			List<ClassHourRequest> listOfClassHourRequest) {

		List<ClassHour> listOfClassHour = new ArrayList<ClassHour>();

		for (ClassHourRequest classHourRequest : listOfClassHourRequest) {

			ClassHour classHour = classHourRepository.findById(classHourRequest.getClassHourId())
					.orElseThrow(() -> new ClassHourNotFoundException("class not found"));

			User teacher = userRepository.findById(classHourRequest.getTeacherId())
					.orElseThrow(() -> new IdNotFoundException("id not found"));

			Subject subject = subjectRepository.findById(classHourRequest.getSubjectId())
					.orElseThrow(() -> new SubjectNotFoundException("subject not found"));

			if (teacher.getUserRole().equals(UserRole.TEACHER)) {

				if (teacher.getSubject().equals(subject)) {

					if (teacher.getListOfAcademicPrograms().contains(classHour.getAcademicPrograms())) {

						LocalDateTime classBeginsAt = classHour.getClassBeginsAt();
						LocalDateTime classEndsAt = classHour.getClassEndsAt();

						LocalDateTime currentDateTime = LocalDateTime.now();

						if (isRoomReserved(classBeginsAt, classEndsAt, classHourRequest.getClassRoomNumber())) {
							throw new RoomAlreadyAssignedException("room already reserved");
						}

						if (!classHour.getClassStatus().equals(ClassStatus.BREAK_TIME) &&

								!classHour.getClassStatus().equals(ClassStatus.LUNCH_TIME)) {

							if (currentDateTime.isAfter(classBeginsAt) && currentDateTime.isBefore(classEndsAt)) {
								classHour.setUser(teacher);
								classHour.setClassRoomNumber(classHourRequest.getClassRoomNumber());
								classHour.setSubject(subject);
								classHour.setClassStatus(ClassStatus.ONGOING);
							} else if (currentDateTime.isBefore(classBeginsAt)) {
								classHour.setUser(teacher);
								classHour.setClassRoomNumber(classHourRequest.getClassRoomNumber());
								classHour.setSubject(subject);
								classHour.setClassStatus(ClassStatus.UPCOMING);
							} else {
								classHour.setUser(teacher);
								classHour.setClassRoomNumber(classHourRequest.getClassRoomNumber());
								classHour.setSubject(subject);
								classHour.setClassStatus(ClassStatus.COMPLETED);
							}
							listOfClassHour.add(classHour);
							classHourRepository.save(classHour);
						} else {
							throw new ClassCannotAssignedException(
									"class hour cannot be assiged to break time or lunch time");
						}
					} else {
						throw new AcademicProgramNotAssignedException("academic program not assigned");
					}
				} else {
					throw new SubjectNotAssignedToTeacherException("subject not assigned");
				}
			} else {
				throw new TeacherNotFoundByIdException("teacher not found");
			}
		}

		return ResponseEntityProxy.setResponseStructure(HttpStatus.OK, "class hour updated successfully",
				mapToClassHourResponse(listOfClassHour));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<ClassHourResponse>>> generateClassHourForNextWeek(int programId) {

		return academicProgramRepository.findById(programId).map(academicProgram -> {
			List<ClassHour> currentWeekClassHours = new ArrayList<>();

			LocalDateTime endDayOfPreviousWeek = academicProgram.getListOfClassHours().getLast().getClassBeginsAt();

			if (endDayOfPreviousWeek == null)
				throw new PreviousClassHourNotFoundException("previous class hour not found");

			classHourRepository.findByClassBeginsAtAfter(endDayOfPreviousWeek.minusDays(6)).forEach(classHour -> {
				currentWeekClassHours.add(ClassHour.builder().classBeginsAt(classHour.getClassBeginsAt().plusWeeks(1))
						.classEndsAt(classHour.getClassEndsAt().plusWeeks(1))
						.academicPrograms(classHour.getAcademicPrograms())
						.classRoomNumber(classHour.getClassRoomNumber()).classStatus(classHour.getClassStatus())
						.subject(classHour.getSubject()).user(classHour.getUser()).build());
			});

			classHourRepository.saveAll(currentWeekClassHours);

			return ResponseEntityProxy.setResponseStructure(HttpStatus.CREATED, "class hour generated successfully",
					mapToClassHourResponse(currentWeekClassHours));

		}).orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
	}

	public void classHourGen(int programId, LocalDateTime now) {

		if (!classHourRepository.existsByClassBeginsAt(
				now.with(academicProgramRepository.findById(programId).get().getSchool().getSchedule().getOpensAt()))) {
			System.out.println("hello 2");
			generateClassHourForNextWeek(programId);
			System.out.println("hello 3");

		}
	}

}
