package com.example.dateon.Controllers;

import com.example.dateon.Models.Question;
import com.example.dateon.Service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.dateon.Service.UserServices;
import com.example.dateon.Models.Users;

@RestController
@RequestMapping("/users/{userId}/questions")
public class QuestionController {
    @Autowired
    QuestionService questionService;
    
    @Autowired
    UserServices userServices;

    @PostMapping
    public ResponseEntity<String> questionSaver(
            @PathVariable int userId,
            @RequestBody Question q,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Users currentUser = userServices.getUserByMail(authentication.getName());
        if (currentUser == null || currentUser.getId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        questionService.savequestions(userId, q);
        return ResponseEntity.ok("Saved");
    }
}
