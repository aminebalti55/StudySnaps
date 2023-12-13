package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.GroupQuizSessionScore;
import com.example.studysnaps.entities.UserGroupQuizSessionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupQuizSessionScoreRepository extends JpaRepository<GroupQuizSessionScore, UserGroupQuizSessionKey> {
}