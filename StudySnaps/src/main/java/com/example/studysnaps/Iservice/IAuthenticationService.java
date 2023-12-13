package com.example.studysnaps.Iservice;

import com.example.studysnaps.dto.JwtAuthenticationResponse;
import com.example.studysnaps.dto.LoginRequest;
import com.example.studysnaps.dto.RegisterRequest;
import com.example.studysnaps.entities.User;


public interface IAuthenticationService {
    User register(RegisterRequest registerRequest);
    public JwtAuthenticationResponse login(LoginRequest loginRequest);
}
