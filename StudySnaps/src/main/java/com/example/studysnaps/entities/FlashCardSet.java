package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class FlashCardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer setId;
    private String title;

    @OneToMany(mappedBy = "flashCardSet")
    private List<FlashCard> cards;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDoc;

}
