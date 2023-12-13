package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Integer> {
}