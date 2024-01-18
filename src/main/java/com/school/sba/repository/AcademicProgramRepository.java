package com.school.sba.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.sba.entity.AcademicProgram;

@Repository
public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Integer>{
	
	@Query(nativeQuery = true, value = "SELECT * FROM school_management_system.academic_program WHERE school_school_id = :foreignKey")
	List<AcademicProgram> findAllBySchool(int foreignKey);
}
