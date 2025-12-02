package com.example.dateon.Models;

import lombok.Data;

@Data
public class KafkaUserInput {
    private int id;
    private double score;
    private boolean lock;
    private String gender;
}
