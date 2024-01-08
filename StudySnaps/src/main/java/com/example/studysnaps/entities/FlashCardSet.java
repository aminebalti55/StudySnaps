package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
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
