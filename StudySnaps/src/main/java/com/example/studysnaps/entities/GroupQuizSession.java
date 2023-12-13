package com.example.studysnaps.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
public class GroupQuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sessionId;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private StudyGroup groupId;

    @ManyToOne
    @JoinColumn(name = "quizId")
    private Quiz quiz;

    @ElementCollection
    @CollectionTable(name = "session_scores", joinColumns = @JoinColumn(name = "sessionId"))
    @MapKeyJoinColumn(name = "userId")
    @Column(name = "score")
    private Map<User, Integer> teamScores;

    @OneToMany(mappedBy = "groupQuizSession")
    private List<GroupQuizSessionScore> sessionScores;

}
