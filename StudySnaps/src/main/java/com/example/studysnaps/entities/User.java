package com.example.studysnaps.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
public class User  implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String firstName;
    private String lastName;
    private String username;
    @Column(nullable = false)
    private String password;
    private String email;

    private Role role;


    @ManyToMany(mappedBy = "members")
    private List<StudyGroup> studyGroups;

    @OneToMany(mappedBy = "user")
    private List<GroupQuizSessionScore> groupQuizSessionScores;


    @OneToMany(mappedBy = "user")
    private List<UserProgressTracking> progressTrackingList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}


