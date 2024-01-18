package com.school.sba.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.Subject;
import com.school.sba.exception.AcademicProgramNotFoundException;
import com.school.sba.repository.AcademicProgramRepository;
import com.school.sba.repository.SubjectRepository;
import com.school.sba.requestdto.SubjectRequest;
import com.school.sba.responsedto.AcademicProgramResponse;
import com.school.sba.service.SubjectService;
import com.school.sba.util.ResponseStructure;

@Service
public class SubjectServiceImpl implements SubjectService{

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private AcademicProgramRepository academicProgramRepository;

	@Autowired
	private ResponseStructure<AcademicProgramResponse> structure;
	
	@Autowired
	private AcademicProgramServiceImpl academicProgramServiceImpl;

	@Override
	public ResponseEntity<ResponseStructure<AcademicProgramResponse>> addSubject(int programId, SubjectRequest subjectRequest) {
		return academicProgramRepository.findById(programId)
				.map(academicProgram -> {
					List<Subject> listOfSubjects = new ArrayList<Subject>();
					
					subjectRequest.getSubjectNames().forEach(name -> {
						Subject fetchedSubject = subjectRepository.findBySubjectName(name.toLowerCase()).map(subject -> {
							return subject;
						}).orElseGet( () -> {
							Subject subject = new Subject();
							subject.setSubjectName(name.toLowerCase());
							subjectRepository.save(subject);
							return subject;
						});
						listOfSubjects.add(fetchedSubject);
					});
					
					academicProgram.setListOfSubject(listOfSubjects);
					academicProgramRepository.save(academicProgram);
					
					structure.setStatus(HttpStatus.CREATED.value());
					structure.setMessage("subjects have been updated successfully");
					structure.setData(academicProgramServiceImpl.mapToAcademicProgramResponse(academicProgram));
					
					return new ResponseEntity<ResponseStructure<AcademicProgramResponse>>(structure, HttpStatus.CREATED);
					
				})
				.orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));

	}

	@Override
	public ResponseEntity<ResponseStructure<AcademicProgramResponse>> updateSubject(int programId,
			SubjectRequest subjectRequest) {
		
		return academicProgramRepository.findById(programId)
		.map(academicProgram -> {
			
			List<Subject> listOfSubjects = new ArrayList<Subject>();
			
			subjectRequest.getSubjectNames().forEach(name -> {
				subjectRepository.findAll().forEach(nameFromDB -> {
					if(name.equalsIgnoreCase(nameFromDB.getSubjectName())) {
						listOfSubjects.add(nameFromDB);
					}
					else {
						Subject subject = new Subject();
						subject.setSubjectName(name.toLowerCase());
						subjectRepository.save(subject);
						listOfSubjects.add(subject);
					}
				});
			});
			academicProgram.setListOfSubject(listOfSubjects);
			academicProgramRepository.save(academicProgram);
			
			structure.setStatus(HttpStatus.CREATED.value());
			structure.setMessage("subjects have been updated successfully");
			structure.setData(academicProgramServiceImpl.mapToAcademicProgramResponse(academicProgram));
			
			return new ResponseEntity<ResponseStructure<AcademicProgramResponse>>(structure, HttpStatus.CREATED);
			
		})
		.orElseThrow(() -> new AcademicProgramNotFoundException("academic program not found"));
	
	}


}
