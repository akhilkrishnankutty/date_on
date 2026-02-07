package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaProducer;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import com.example.dateon.Repositories.ChatMessageRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class UserServices {

    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    UserRepo repo;
    @Autowired
    KafkaProducer kafkaProducer;
    @Autowired
    ChatMessageRepository chatMessageRepository;

    public Users createNewUser(@org.jetbrains.annotations.NotNull Users u1) {
        u1.setPassword(encoder.encode(u1.getPassword()));
        u1.setLock(true);
        u1.setStatus("REGISTERED");
        repo.save(u1);
        // kafkaProducer.available(u1); // Removed: Triggered after completion now
        return u1;
    }

    public Users getUserByMail(String mail) {
        return repo.findByMail(mail);
    }

    public Users getUserById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Users completeProfile(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("AI_PROCESSING");
        user.setLock(false); // Unlock user so they can be found by other users in matching queue
        repo.save(user);
        kafkaProducer.available(user);
        return user;
    }

    public Users unmatchUser(int userId) {
        Users currentUser = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"MATCHED".equals(currentUser.getStatus()) || currentUser.getLoid() == 0) {
            throw new RuntimeException("User is not currently matched");
        }

        int matchedUserId = currentUser.getLoid();

        // Delete all chat messages between these users
        chatMessageRepository.deleteConversation(userId, matchedUserId);
        Users matchedUser = repo.findById(matchedUserId)
                .orElseThrow(() -> new RuntimeException("Matched user not found"));

        // Add each other to pastMatches to prevent future re-matching
        addToPastMatches(currentUser, matchedUserId);
        addToPastMatches(matchedUser, userId);

        // Reset both users' match status
        currentUser.setStatus("MATCH_FINDING");
        currentUser.setLoid(0);
        currentUser.setMatchTime(null);
        currentUser.setLock(false);

        matchedUser.setStatus("MATCH_FINDING");
        matchedUser.setLoid(0);
        matchedUser.setMatchTime(null);
        matchedUser.setLock(false);

        // Clear answers
        currentUser.setAnswerToMatchQuestion(null);
        matchedUser.setAnswerToMatchQuestion(null);

        repo.save(currentUser);
        repo.save(matchedUser);

        // Trigger matching for both users
        kafkaProducer.available(currentUser);
        kafkaProducer.available(matchedUser);

        return currentUser;
    }

    private void addToPastMatches(Users user, int matchedUserId) {
        String currentPastMatches = user.getPastMatches();
        String idStr = String.valueOf(matchedUserId);

        // Check if already in past matches
        if (currentPastMatches != null && !currentPastMatches.isBlank()) {
            String[] ids = currentPastMatches.split(",");
            for (String id : ids) {
                if (id.trim().equals(idStr)) {
                    return; // Already exists
                }
            }
            user.setPastMatches(currentPastMatches + "," + idStr);
        } else {
            user.setPastMatches(idStr);
        }
    }

    @SuppressWarnings("null")
    public Users updateUser(int id, Users updatedData) {
        Users existingUser = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields that are allowed to be changed
        if (updatedData.getName() != null && !updatedData.getName().isEmpty())
            existingUser.setName(updatedData.getName());
        if (updatedData.getBio() != null)
            existingUser.setBio(updatedData.getBio());
        if (updatedData.getWorkplace() != null)
            existingUser.setWorkplace(updatedData.getWorkplace());
        if (updatedData.getLocation() != null)
            existingUser.setLocation(updatedData.getLocation());
        if (updatedData.getInterests() != null)
            existingUser.setInterests(updatedData.getInterests());

        // Don't update email, password, or status here for security/logic reasons

        return repo.save(existingUser);
    }

    @org.springframework.transaction.annotation.Transactional
    public Users uploadProfilePicture(int userId, MultipartFile file) throws IOException {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfilePicture(file.getBytes());
        user.setProfilePictureContentType(file.getContentType());
        return repo.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public byte[] getProfilePicture(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getProfilePicture();
    }

    @org.springframework.transaction.annotation.Transactional
    public String getProfilePictureContentType(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getProfilePictureContentType();
    }

    public boolean toggleAccountPause(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isPaused = !user.isPaused();
        user.setPaused(isPaused);

        if (isPaused) {
            // Pausing: Remove from matching pool
            user.setLock(true); // Ensure they are locked out of matching
            if ("MATCHED".equals(user.getStatus()) && user.getLoid() != 0) {
                // Unmatch if currently matched (optional, business logic choice)
                // For now, let's keep them matched but paused?
                // Or better: Force unmatch so they don't block the other person.
                try {
                    unmatchUser(userId);
                } catch (Exception e) {
                    // Ignore if unmatch fails (e.g. race condition)
                }
            }
            user.setStatus("PAUSED");
        } else {
            // Unpausing: Re-enter matching pool
            user.setStatus("MATCH_FINDING");
            user.setLock(false);
            kafkaProducer.available(user); // Trigger matching immediately
        }

        repo.save(user);
        return isPaused;
    }

    public Users answerMatchQuestion(int userId, String answer) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!"MATCHED".equals(user.getStatus()) || user.getLoid() == 0) {
            throw new RuntimeException("User is not matched, cannot answer question");
        }

        user.setAnswerToMatchQuestion(answer);
        return repo.save(user);
    }
}
