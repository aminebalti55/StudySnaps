package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer quizId;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDoc;

    private String title;

    @ElementCollection
    private List<String> questions;

    @ElementCollection
    private List<String> answers;

    @ElementCollection
    private List<String> userResponses;

}
