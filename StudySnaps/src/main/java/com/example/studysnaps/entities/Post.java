package com.example.studysnaps.entities;

import jakarta.persistence.*;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;
    private String text;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private StudyGroup studyGroup;

}
