package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.GroupQuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupQuizSessionRepository extends JpaRepository<GroupQuizSession, Integer> {
}