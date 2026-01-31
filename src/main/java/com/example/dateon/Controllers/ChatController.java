package com.example.dateon.Controllers;

import com.example.dateon.Models.ChatMessage;
import com.example.dateon.Repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private com.example.dateon.Repo.UserRepo userRepo;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(chatMessage);

        // Resolve Recipient Email
        String recipientEmail = userRepo.findById(chatMessage.getRecipientId())
                .map(com.example.dateon.Models.Users::getMail)
                .orElse(null);

        // Resolve Sender Email
        String senderEmail = userRepo.findById(chatMessage.getSenderId())
                .map(com.example.dateon.Models.Users::getMail)
                .orElse(null);

        if (recipientEmail != null) {
            // Send to recipient
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/messages",
                    saved);
        }

        if (senderEmail != null) {
            // Send back to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    senderEmail,
                    "/queue/messages",
                    saved);
        }
    }

    @GetMapping("/messages/{user1Id}/{user2Id}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable int user1Id,
            @PathVariable int user2Id) {
        return ResponseEntity.ok(chatMessageRepository.findConversation(user1Id, user2Id));
    }
}
