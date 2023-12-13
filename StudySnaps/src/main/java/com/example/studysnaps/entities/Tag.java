package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<PDFDocument> documents;

}
