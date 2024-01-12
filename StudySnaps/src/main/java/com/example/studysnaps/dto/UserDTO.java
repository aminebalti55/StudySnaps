package com.example.studysnaps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;


}
