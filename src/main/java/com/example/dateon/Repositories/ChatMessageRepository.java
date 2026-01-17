package com.example.dateon.Repositories;

import com.example.dateon.Models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1Id AND m.recipientId = :user2Id) OR (m.senderId = :user2Id AND m.recipientId = :user1Id) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(int user1Id, int user2Id);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE (m.senderId = :user1Id AND m.recipientId = :user2Id) OR (m.senderId = :user2Id AND m.recipientId = :user1Id)")
    void deleteConversation(int user1Id, int user2Id);
}
