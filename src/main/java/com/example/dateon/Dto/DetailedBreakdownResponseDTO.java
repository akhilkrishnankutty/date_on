package com.example.dateon.Dto;

import lombok.Data;

@Data
public class DetailedBreakdownResponseDTO {
    private double compatibility_score;
    private double values_score;
    private double lifestyle_score;
    private double communication_score;
    private String values_subtitle;
    private String lifestyle_subtitle;
    private String communication_subtitle;
}
