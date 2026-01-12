package com.example.dateon.Service;

import com.example.dateon.Models.Question;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.QuestionRepo;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    @Autowired
    QuestionRepo questionRepo;

    @Autowired
    UserRepo userRepo;

    public void savequestions(int userId, Question q1) {
        Users user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        q1.setUser(user);
        questionRepo.save(q1);
    }
}
