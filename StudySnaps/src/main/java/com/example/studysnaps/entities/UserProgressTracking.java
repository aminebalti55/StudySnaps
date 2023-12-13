package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserProgressTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trackingId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDocument;

    @ManyToOne
    @JoinColumn(name = "quizId")
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "flashCardSetId")
    private FlashCardSet flashCardSet;

    private Double progressPercentage;
    private LocalDateTime lastAccessed;
}
