package com.example.dateon.Repo;

import com.example.dateon.Models.Question;
import com.example.dateon.Models.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Integer> {
    List<Question> findByUserAndQuestionText(Users user, String questionText);
}

