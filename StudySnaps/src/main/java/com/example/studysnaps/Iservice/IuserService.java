package com.example.studysnaps.Iservice;

import com.example.studysnaps.dto.JwtAuthenticationResponse;
import com.example.studysnaps.dto.LoginRequest;
import com.example.studysnaps.dto.RegisterRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IuserService {

    UserDetailsService userDetailsService();

}
