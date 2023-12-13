package com.example.studysnaps.services;

import com.example.studysnaps.Iservice.IuserService;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.dto.JwtAuthenticationResponse;
import com.example.studysnaps.dto.LoginRequest;
import com.example.studysnaps.dto.RegisterRequest;
import com.example.studysnaps.entities.Role;
import com.example.studysnaps.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@RequiredArgsConstructor

@Service
public class UserService implements IuserService{


@Autowired
    UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }



}
