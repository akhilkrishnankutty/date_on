package com.example.dateon.Service;

import com.example.dateon.Models.ChatMessage;
import com.example.dateon.Models.Users;
import com.example.dateon.Repositories.ChatMessageRepository;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Persist message and notify both sender and recipient via WebSocket
     */
    @Transactional
    public void processAndSendMessage(ChatMessage chatMessage) {
        // 1. Timestamp and Persist
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(chatMessage);

        // 2. Notify Recipient
        notifyUser(saved.getRecipientId(), saved);

        // 3. Notify Sender (Confirmation/Echo)
        notifyUser(saved.getSenderId(), saved);
    }

    private void notifyUser(int userId, ChatMessage message) {
        userRepo.findById(userId)
                .map(Users::getMail)
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(
                        email,
                        "/queue/messages",
                        message));
    }

    public List<ChatMessage> getConversation(int user1Id, int user2Id) {
        return chatMessageRepository.findConversation(user1Id, user2Id);
    }
}
