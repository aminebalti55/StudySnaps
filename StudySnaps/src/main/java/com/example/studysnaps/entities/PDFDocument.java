package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Getter
@Setter
public class PDFDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer docId;
    private String title;


    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "docId"),
            inverseJoinColumns = @JoinColumn(name = "tagId"))
    private List<Tag> tags;



    @OneToMany(mappedBy = "pdfDoc")
    private List<FlashCardSet> flashCardSet;

    @OneToMany(mappedBy = "pdfDoc")
    private List<Quiz> quiz;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Lob
    private byte[] content;
}
