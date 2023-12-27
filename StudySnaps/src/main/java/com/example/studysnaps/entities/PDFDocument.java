package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Entity
@Getter
@Setter
public class PDFDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer docId;
    private String title;


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

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;


}
