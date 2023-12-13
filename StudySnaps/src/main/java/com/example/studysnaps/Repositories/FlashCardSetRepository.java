package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.FlashCardSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashCardSetRepository extends JpaRepository<FlashCardSet, Integer> {
}