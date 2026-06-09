package com.example.dateon.Service;

import com.example.dateon.Models.Question;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.QuestionRepo;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionRepo questionRepo;

    @Autowired
    UserRepo userRepo;

    public void savequestions(int userId, Question q1) {
        Users user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Question> existingQuestions = questionRepo.findByUserAndQuestionText(user, q1.getQuestionText());
        if (existingQuestions != null && !existingQuestions.isEmpty()) {
            // Update the primary question entry
            Question primary = existingQuestions.get(0);
            primary.setAnswerText(q1.getAnswerText());
            questionRepo.save(primary);

            // Clean up any existing duplicates that were created because of this bug
            if (existingQuestions.size() > 1) {
                for (int i = 1; i < existingQuestions.size(); i++) {
                    questionRepo.delete(existingQuestions.get(i));
                }
            }
        } else {
            q1.setUser(user);
            questionRepo.save(q1);
        }
    }
}
