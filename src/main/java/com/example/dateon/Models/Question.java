package com.example.dateon.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Entity
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String questionText;
    private String answerText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usr_id", nullable = false)
    private Users user;
}
