package com.school.sba.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.AcademicProgram;
import com.school.sba.entity.Subject;
import com.school.sba.exception.SchoolNotFoundByIdException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.SchoolRepository;
import com.school.sba.requestdto.AcademicProgramRequest;
import com.school.sba.responsedto.AcademicProgramResponse;
import com.school.sba.service.AcademicProgramService;
import com.school.sba.util.ResponseStructure;


@Service
public class AcademicProgramServiceImpl implements AcademicProgramService{

	@Autowired
	private AcademicProgramRepository academicProgramRepository;

	@Autowired
	private SchoolRepository schoolRepository;

	@Autowired
	private ResponseStructure<AcademicProgramResponse> structure;

	@Autowired
	private ResponseStructure<List<AcademicProgramResponse>> listStructure;

	public AcademicProgramResponse mapToAcademicProgramResponse(AcademicProgram academicProgram) {

		List<String> subjects = new ArrayList<String>();
		List<Subject> listOfSubject = academicProgram.getListOfSubject();

		if(listOfSubject != null) {
			listOfSubject.forEach(sub -> {
				subjects.add(sub.getSubjectName());
			});
		}

		return AcademicProgramResponse.builder()
				.programId(academicProgram.getProgramId())
				.programType(academicProgram.getProgramType())
				.programName(academicProgram.getProgramName())
				.programBeginsAt(academicProgram.getProgramBeginsAt())
				.programEndsAt(academicProgram.getProgramEndsAt())
				.listOfSubjects(subjects)
				.build();
	}

	private AcademicProgram mapToAcademicProgram(AcademicProgramRequest academicProgramRequest) {
		return AcademicProgram.builder()
				.programType(academicProgramRequest.getProgramType())
				.programName(academicProgramRequest.getProgramName())
				.programBeginsAt(academicProgramRequest.getProgramBeginsAt())
				.programEndsAt(academicProgramRequest.getProgramEndsAt())
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<AcademicProgramResponse>> createProgram(int schoolId,
			AcademicProgramRequest academicProgramRequest) {

		return schoolRepository.findById(schoolId)
				.map(school -> {
					AcademicProgram academicProgram = academicProgramRepository.save(mapToAcademicProgram(academicProgramRequest));

					school.getListOfAcademicPrograms().add(academicProgram);

					school = schoolRepository.save(school);
					academicProgram.setSchool(school);

					academicProgram = academicProgramRepository.save(academicProgram);

					structure.setStatus(HttpStatus.CREATED.value());
					structure.setMessage("Academic program created successfully");
					structure.setData(mapToAcademicProgramResponse(academicProgram));

					return new ResponseEntity<ResponseStructure<AcademicProgramResponse>>(structure, HttpStatus.CREATED);
				})
				.orElseThrow(() -> new SchoolNotFoundByIdException("school not found"));

	}

	@Override
	public ResponseEntity<ResponseStructure<List<AcademicProgramResponse>>> findAllAcademicProgram(int schoolId) {

		return schoolRepository.findById(schoolId)
				.map(school -> {
					List<AcademicProgram> listOfAcadmicProgram = academicProgramRepository.findAll();

					List<AcademicProgramResponse> listOfAcademicProgramResponse = listOfAcadmicProgram.stream()
							.map(this::mapToAcademicProgramResponse)
							.collect(Collectors.toList());

					if(listOfAcadmicProgram.isEmpty()) {
						listStructure.setStatus(HttpStatus.NO_CONTENT.value());
						listStructure.setMessage("no programs has been found");
						listStructure.setData(listOfAcademicProgramResponse);

						return new ResponseEntity<ResponseStructure<List<AcademicProgramResponse>>>(listStructure, HttpStatus.NO_CONTENT);
					}
					else {
						listStructure.setStatus(HttpStatus.FOUND.value());
						listStructure.setMessage("found list of academic programs");
						listStructure.setData(listOfAcademicProgramResponse);

						return new ResponseEntity<ResponseStructure<List<AcademicProgramResponse>>>(listStructure, HttpStatus.FOUND);
					}
				})
				.orElseThrow(() -> new SchoolNotFoundByIdException("school not found"));
	}


}
