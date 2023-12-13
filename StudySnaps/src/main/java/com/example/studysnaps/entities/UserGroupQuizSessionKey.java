package com.example.studysnaps.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
@Embeddable

public class UserGroupQuizSessionKey implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "session_id")
    private Integer sessionId;

}
