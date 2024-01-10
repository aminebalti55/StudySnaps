package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer quizId;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDoc;

    private String title;

    @ElementCollection
    @Column(length = 5000)

    private List<String> questions;

    @ElementCollection
    @Column(length = 5000)
    private List<String> answers;

    @ElementCollection
    private List<String> userResponses;

    @ElementCollection
    private List<String> userResponses2;

    @OneToOne(mappedBy = "quiz")
    private Room room;

    private Duration quizDuration;

    private LocalDateTime quizStartTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
