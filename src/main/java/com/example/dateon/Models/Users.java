package com.example.dateon.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int loid;
    private String name;
    private String mail;
    private double number;
    private String gender;
    private String dob;
    private int age;
    private String workplace;
    private String bio;
    private String location;
    private double compatibilityScore;
    private String interests;
    @Column(name = "\"lock\"")
    private boolean lock;
    private boolean isPaused;
    private String customQuestion;
    private String answerToMatchQuestion;
    private String password;
    private java.time.LocalDateTime matchTime;
    private String status; // REGISTERED, AI_PROCESSING, MATCH_FINDING, MATCHED

    @Column(columnDefinition = "TEXT")
    private String pastMatches; // Comma-separated list of past matched user IDs

    private String aiMatch;
    private java.time.LocalDateTime matchCooldownUntil;

    private String fcmToken;

    @Column(columnDefinition = "TEXT")
    private String profilePictureUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Question> questions = new ArrayList<>();
}
