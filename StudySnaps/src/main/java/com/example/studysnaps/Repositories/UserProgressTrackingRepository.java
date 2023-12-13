package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.UserProgressTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProgressTrackingRepository extends JpaRepository<UserProgressTracking, Integer> {
}