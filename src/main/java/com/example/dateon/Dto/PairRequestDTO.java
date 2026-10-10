package com.example.dateon.Dto;

import lombok.Data;

@Data
public class PairRequestDTO {
    private QuizAnswersDTO user_a;
    private QuizAnswersDTO user_b;

    public PairRequestDTO() {}

    public PairRequestDTO(QuizAnswersDTO user_a, QuizAnswersDTO user_b) {
        this.user_a = user_a;
        this.user_b = user_b;
    }
}
