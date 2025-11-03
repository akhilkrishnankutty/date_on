package com.example.dateon.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String mail;
    private double number;
    private String gender;
    private int age;
    private String location;
    private double compatibilityScore;
    @Column(name = "\"lock\"")
    private boolean lock;

}
