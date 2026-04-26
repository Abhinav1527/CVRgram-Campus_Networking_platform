package com.CVRgram.Model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String department;
    private String headline;
    private String bio;

    private String profilePhotoUrl;
    private String backgroundPhotoUrl;

    private boolean verified;
    private String otp;

    @ElementCollection
    @JsonIgnore
    private Set<Long> followingIds = new HashSet<>();

    @ElementCollection
    @JsonIgnore
    private Set<Long> followerIds = new HashSet<>();

    @ElementCollection
    @JsonIgnore
    private Set<Long> pendingFollowerIds = new HashSet<>();

    @ElementCollection
    @JsonIgnore
    private Set<Long> pendingFollowingIds = new HashSet<>();
}