package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FlashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    @ManyToOne
    @JoinColumn(name = "setId")
    private FlashCardSet flashCardSet;

    @Column(columnDefinition = "TEXT")
    private String definitionText;

    @Column(columnDefinition = "TEXT")
    private String concept;

}
