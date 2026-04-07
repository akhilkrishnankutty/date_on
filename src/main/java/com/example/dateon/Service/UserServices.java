package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaProducer;
import com.example.dateon.Models.UserForgotPass;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserForgotPassRepo;
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
    private CloudinaryService cloudinaryService;

    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    UserRepo repo;
    
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private void broadcastUserUpdate(int userId) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/user.status." + userId, "UPDATE");
        }
    }

    @Autowired
    KafkaProducer kafkaProducer;
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    UserForgotPassRepo forgotPassRepo;

    public Users createNewUser(Users u1) {
        Users existingUser = repo.findByMail(u1.getMail());
        if (existingUser != null) {
            throw new RuntimeException("User with this email already exists");
        }
        u1.setPassword(encoder.encode(u1.getPassword()));
        u1.setLock(true);
        u1.setStatus("REGISTERED"); // Set initial status
        return repo.save(u1);
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
        broadcastUserUpdate(user.getId());
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

        // Reset initiator user's match status to COOLDOWN
        currentUser.setStatus("COOLDOWN");
        currentUser.setMatchCooldownUntil(java.time.LocalDateTime.now().plusDays(5));
        currentUser.setLoid(0);
        currentUser.setMatchTime(null);
        currentUser.setLock(true); // Locked from matching

        matchedUser.setStatus("MATCH_FINDING");
        matchedUser.setLoid(0);
        matchedUser.setMatchTime(null);
        matchedUser.setLock(false);

        // Clear answers
        currentUser.setAnswerToMatchQuestion(null);
        matchedUser.setAnswerToMatchQuestion(null);

        repo.save(currentUser);
        repo.save(matchedUser);

        broadcastUserUpdate(currentUser.getId());
        broadcastUserUpdate(matchedUser.getId());

        // Trigger matching ONLY for target user
        kafkaProducer.available(matchedUser);

        return currentUser;
    }

    public void unlockCooldown(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setMatchCooldownUntil(null);
        user.setStatus("MATCH_FINDING");
        user.setLock(false);
        repo.save(user);
        broadcastUserUpdate(user.getId());
        kafkaProducer.available(user);
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
        String imageUrl = cloudinaryService.uploadFile(file);
        user.setProfilePictureUrl(imageUrl);
        return repo.save(user);
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
        broadcastUserUpdate(user.getId());
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

    public Users saveCustomQuestion(int userId, String question) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setCustomQuestion(question);
        return repo.save(user);
    }

    public String processForgotPassword(String email) {
        Users user = repo.findByMail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Generate temporary password (8 characters)
        String tempPass = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Save to UserForgotPass table
        UserForgotPass forgotPass = new UserForgotPass();
        forgotPass.setMail(email);
        forgotPass.setTempPassword(tempPass);
        forgotPass.setCreatedAt(java.time.LocalDateTime.now());
        forgotPassRepo.save(forgotPass);

        // Update main Users table
        user.setPassword(encoder.encode(tempPass));
        repo.save(user);

        return tempPass;
    }

    public void changePassword(int userId, String oldPassword, String newPassword) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password does not match");
        }
        user.setPassword(encoder.encode(newPassword));
        repo.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(int userId) {
        Users user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Step 1: Unmatch if currently matched to free up the other user
        if ("MATCHED".equals(user.getStatus()) && user.getLoid() != 0) {
            try {
                unmatchUser(userId);
            } catch (Exception e) {
                // Ignore if unmatch fails, we are deleting the user anyway
            }
        }

        // Step 2: Delete all messages involving this user
        chatMessageRepository.deleteAllUserMessages(userId);

        // Step 3: Delete forgot password tokens if any
        java.util.List<UserForgotPass> pwdRequests = forgotPassRepo.findByMail(user.getMail());
        if (pwdRequests != null && !pwdRequests.isEmpty()) {
            forgotPassRepo.deleteAll(pwdRequests);
        }

        // Step 4: Delete user (questions cascade deleted if mapped, profile picture embedded)
        repo.delete(user);
        
        // Broadcast delete update in case UI is active for someone matched with them
        broadcastUserUpdate(userId);
    }
}
