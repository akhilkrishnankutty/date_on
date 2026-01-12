package com.example.dateon.Service;

import com.example.dateon.Models.Question;
import com.example.dateon.Repo.QuestionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    @Autowired
    QuestionRepo questionRepo;
    public void savequestions(int userId, Question q1) {
        questionRepo.save(q1);
    }
}
