package com.example.dateon.Controllers;

import com.example.dateon.Models.Question;
import com.example.dateon.Service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/questions")
public class QuestionController {
    @Autowired
    QuestionService questionService;

    @PostMapping
    public ResponseEntity<String> questionSaver(
            @PathVariable int userId
            @RequestBody Question q) {

        questionService.savequestions(userId, q);
        return ResponseEntity.ok("Saved");
    }
}
