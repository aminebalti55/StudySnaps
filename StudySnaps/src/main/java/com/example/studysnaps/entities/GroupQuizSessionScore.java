package com.example.studysnaps.entities;

import jakarta.persistence.*;

@Entity

public class GroupQuizSessionScore {

    @EmbeddedId
    private UserGroupQuizSessionKey id;

    @ManyToOne
    @MapsId("sessionId") // This corresponds to the name of the attribute in UserGroupQuizSessionKey
    @JoinColumn(name = "session_id")
    private GroupQuizSession groupQuizSession;

    @ManyToOne
    @MapsId("userId") // This corresponds to the name of the attribute in UserGroupQuizSessionKey
    @JoinColumn(name = "user_id")
    private User user;

    private Integer score;
}
