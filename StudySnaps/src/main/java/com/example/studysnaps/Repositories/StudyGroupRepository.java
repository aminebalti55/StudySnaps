package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
}