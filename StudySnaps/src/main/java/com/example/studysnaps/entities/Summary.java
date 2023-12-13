package com.example.studysnaps.entities;

import jakarta.persistence.*;

@Entity
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer summaryId;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDoc;

    private String title;
    private String text;
    private Integer length;
}
