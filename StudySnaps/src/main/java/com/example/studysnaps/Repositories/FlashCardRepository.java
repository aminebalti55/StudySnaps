package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.FlashCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Integer> {
}