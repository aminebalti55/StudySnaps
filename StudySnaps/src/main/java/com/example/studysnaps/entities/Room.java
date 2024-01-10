package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    @OneToMany(mappedBy = "room")
    private List<User> users;

    // A room can have one pdf document that is uploaded by one of the users
    @OneToOne
    @JoinColumn(name = "docId")
    private PDFDocument pdfDoc;

    // A room can have one quiz that is generated from the pdf document
    @OneToOne
    @JoinColumn(name = "quizId")
    private Quiz quiz;

    // A room can have a status that indicates if the game is active or not
    private Boolean isActive;

    // A room can have a winner that is determined by the score and the time
    @OneToOne
    @JoinColumn(name = "userId")
    private User winner;

    @Column(name = "owner_id")
    private Integer ownerId;


}



