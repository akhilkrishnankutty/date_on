package com.example.dateon.Controllers;

import com.example.dateon.Models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.example.dateon.Service.UserServices;
import com.example.dateon.Models.Users;


import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private com.example.dateon.Service.ChatService chatService;

    @Autowired
    private UserServices userServices;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatService.processAndSendMessage(chatMessage);
    }

    @GetMapping("/messages/{user1Id}/{user2Id}")
    public ResponseEntity<?> getChatHistory(
            @PathVariable int user1Id,
            @PathVariable int user2Id,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Users currentUser = userServices.getUserByMail(authentication.getName());
        if (currentUser == null || (currentUser.getId() != user1Id && currentUser.getId() != user2Id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        return ResponseEntity.ok(chatService.getConversation(user1Id, user2Id));
    }
}
