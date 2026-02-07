package com.example.dateon.Dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchRequestDTO {
    private QuizAnswersDTO target_user;
    private List<QuizAnswersDTO> candidates;
}
