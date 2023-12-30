package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.DocumentTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTagRepository extends JpaRepository<DocumentTag, Integer> {
}