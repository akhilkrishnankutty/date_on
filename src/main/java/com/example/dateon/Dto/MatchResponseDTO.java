package com.example.dateon.Dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchResponseDTO {
    private List<MatchResult> results;

    @Data
    public static class MatchResult {
        private int candidate_index;
        private double compatibility_score;
    }
}
