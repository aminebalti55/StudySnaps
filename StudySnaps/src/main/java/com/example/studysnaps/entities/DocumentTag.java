package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DocumentTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentTagId;

    @ManyToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDocument;

    @ManyToOne
    @JoinColumn(name = "tagId")
    private Tag tag;

    @Column(name = "user_id")
    private Integer userId;
}
