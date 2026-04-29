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
        // 0. Validate if chatting is allowed (Both must have answered custom questions)
        Users sender = userRepo.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Users recipient = userRepo.findById(chatMessage.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if ((sender.getAnswerToMatchQuestion() == null || sender.getAnswerToMatchQuestion().isBlank()) ||
                (recipient.getAnswerToMatchQuestion() == null || recipient.getAnswerToMatchQuestion().isBlank())) {
            throw new RuntimeException("Both users must answer the custom question to unlock chat.");
        }

        // 1. Timestamp and Persist
        chatMessage.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(chatMessage);

        // 2. Notify Recipient via WebSocket
        notifyUser(saved.getRecipientId(), saved);
        
        // 2b. Notify Recipient via FCM Push Notification
        sendPushNotification(recipient, saved);

        // 3. Notify Sender (Confirmation/Echo)
        notifyUser(saved.getSenderId(), saved);
    }

    private void notifyUser(int userId, ChatMessage message) {
        userRepo.findById(userId)
                .map(Users::getMail)
                .ifPresent(email -> {
                    System.out.println("Notifying user " + email + " at /queue/messages");
                    messagingTemplate.convertAndSendToUser(
                            java.util.Objects.requireNonNull(email),
                            "/queue/messages",
                            java.util.Objects.requireNonNull(message));
                });
    }

    public List<ChatMessage> getConversation(int user1Id, int user2Id) {
        return chatMessageRepository.findConversation(user1Id, user2Id);
    }

    private void sendPushNotification(Users recipient, ChatMessage chatMessage) {
        if (recipient.getFcmToken() != null && !recipient.getFcmToken().isEmpty()) {
            try {
                com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                        .setToken(recipient.getFcmToken())
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle("New Message ✨")
                                .setBody("You have a new message from your match!")
                                .build())
                        .putData("type", "chat")
                        .putData("senderId", String.valueOf(chatMessage.getSenderId()))
                        .build();
                com.google.firebase.messaging.FirebaseMessaging.getInstance().send(message);
                System.out.println("FCM Notification sent to recipient: " + recipient.getId());
            } catch (Exception e) {
                System.err.println("Failed to send FCM: " + e.getMessage());
            }
        }
    }
}
