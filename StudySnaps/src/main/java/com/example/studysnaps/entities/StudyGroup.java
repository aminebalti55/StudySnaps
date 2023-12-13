package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupId;
    private String name;

    @ManyToMany
    @JoinTable(
            name = "study_group_members",
            joinColumns = @JoinColumn(name = "groupId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    private List<User> members;

    @OneToMany(mappedBy = "studyGroup")
    private List<Post> posts;

    @OneToMany(mappedBy = "groupId")
    private List<GroupQuizSession> groupQuizSessions;
}
