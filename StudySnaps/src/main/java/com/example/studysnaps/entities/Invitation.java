package com.example.studysnaps.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Invitation {
    private String senderUsername;
    private String recipientUsername;
    // additional fields
}