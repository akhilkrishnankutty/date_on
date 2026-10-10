package com.example.dateon.Dto;

import lombok.Data;

@Data
public class CompatibilityBreakdownDTO {
    private int overallScore;
    private int valuesScore;
    private int lifestyleScore;
    private int communicationScore;
    private String valuesSubtitle;
    private String lifestyleSubtitle;
    private String communicationSubtitle;
    private String matchName;

    public CompatibilityBreakdownDTO() {}

    public CompatibilityBreakdownDTO(
            int overallScore,
            int valuesScore,
            int lifestyleScore,
            int communicationScore,
            String valuesSubtitle,
            String lifestyleSubtitle,
            String communicationSubtitle,
            String matchName) {
        this.overallScore = overallScore;
        this.valuesScore = valuesScore;
        this.lifestyleScore = lifestyleScore;
        this.communicationScore = communicationScore;
        this.valuesSubtitle = valuesSubtitle;
        this.lifestyleSubtitle = lifestyleSubtitle;
        this.communicationSubtitle = communicationSubtitle;
        this.matchName = matchName;
    }
}
