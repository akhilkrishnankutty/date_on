package com.example.dateon.Controllers;

import com.example.dateon.Models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private com.example.dateon.Service.ChatService chatService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatService.processAndSendMessage(chatMessage);
    }

    @GetMapping("/messages/{user1Id}/{user2Id}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable int user1Id,
            @PathVariable int user2Id) {
        return ResponseEntity.ok(chatService.getConversation(user1Id, user2Id));
    }
}
