package com.example.dateon.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int age;
    private String location;
    private double compatibilityScore;
    @Column(name = "\"lock\"")
    private boolean lock;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Question> questions = new ArrayList<>();
}
