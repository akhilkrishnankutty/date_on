package com.example.dateon.Controllers;

import com.example.dateon.Models.QuizQuestion;
import com.example.dateon.Repositories.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @GetMapping("/questions")
    public List<QuizQuestion> getAllQuestions() {
        return quizQuestionRepository.findAll();
    }
}
