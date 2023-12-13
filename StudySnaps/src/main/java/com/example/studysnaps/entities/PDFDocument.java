package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;
@Entity
public class PDFDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer docId;
    private String title;
    private Integer numberOfPages;
    @Column(length = 10000)
    private String textContent;

    @ManyToMany
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "docId"),
            inverseJoinColumns = @JoinColumn(name = "tagId"))
    private List<Tag> tags;

    @OneToMany(mappedBy = "pdfDoc")
    private List<Summary> summary;

    @OneToMany(mappedBy = "pdfDoc")
    private List<FlashCardSet> flashCardSet;

    @OneToMany(mappedBy = "pdfDoc")
    private List<Quiz> quiz;

}
