package com.example.studysnaps.entities;

import jakarta.persistence.*;

@Entity
public class FlashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    @ManyToOne
    @JoinColumn(name = "setId")
    private FlashCardSet flashCardSet;

    private String questionText;
    private String answerText;

}
