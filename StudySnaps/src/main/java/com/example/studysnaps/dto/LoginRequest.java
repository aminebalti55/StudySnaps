package com.example.studysnaps.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
