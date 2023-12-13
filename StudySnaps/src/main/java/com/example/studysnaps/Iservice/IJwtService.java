package com.example.studysnaps.Iservice;

import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails);

    Boolean validateToken(String token, UserDetails userDetails);

}
