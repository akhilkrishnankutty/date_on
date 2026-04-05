package com.example.dateon.Kafka;

import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Service.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    Matcher matcher;
    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private com.example.dateon.Repo.UserRepo userRepo;

    // LISTENER 1: Consume Free_user
    @Autowired
    private com.example.dateon.Service.AIService aiService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private void broadcastUserUpdate(int userId) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/user.status." + userId, "UPDATE");
        }
    }


    @KafkaListener(topics = "Free_user", groupId = "free-user-group")
    public void firstai(Users user) {
        try {
            System.out.println("Consumed from Free_user: " + user.getName());

            // REAL AI INTEGRATION (OPTIMIZED)
            // Step 1: Find candidates (Filtering by opposite gender and distinct ID)
            List<Users> allUsers = userRepo.findAll();

            List<Users> candidates = allUsers.stream()
                    .filter(u -> u.getId() != user.getId()) // Not self
                    .filter(u -> u.getGender() != null && !u.getGender().equalsIgnoreCase(user.getGender())) // Opposite
                                                                                                             // gender
                                                                                                             // (simple
                                                                                                             // logic)
                    // Add location filter if needed: .filter(u ->
                    // u.getLocation().equalsIgnoreCase(user.getLocation()))
                    .toList();

            // Re-fetch from DB to check current pause status
            Users freshUser = userRepo.findById(user.getId()).orElse(user);
            if (freshUser.isPaused()) {
                System.out.println("User " + freshUser.getName() + " is paused. Skipping AI processing status update.");
                return;
            }

            double maxScore = 0.0;
            try {
                if (!candidates.isEmpty()) {
                    System.out.println("Found " + candidates.size() + " candidates for " + freshUser.getName());
                    // Batch Call to AI
                    maxScore = aiService.getCompatibilityScore(freshUser, candidates);
                } else {
                    System.out.println("No matching candidates found.");
                }
            } catch (Exception e) {
                System.err.println("AI Service Failed: " + e.getMessage());
                e.printStackTrace();
            }

            // ALWAYS update status, even if AI failed, but ONLY if not paused
            freshUser.setCompatibilityScore(maxScore);
            freshUser.setStatus("MATCH_FINDING");
            userRepo.save(freshUser);
            broadcastUserUpdate(freshUser.getId());

            System.out.println("AI Processing Complete for User ID: " + freshUser.getId() + ", Max Score: " + maxScore);

            KafkaUserInput input = new KafkaUserInput();
            input.setId(freshUser.getId());
            input.setScore(maxScore);
            input.setGender(freshUser.getGender());
            input.setLock(true);

            kafkaProducer.checker(input);

        } catch (Exception e) {
            System.err.println("Error processing Free_user message");
            e.printStackTrace();
        }
    }

    // LISTENER 2: Consume compatable
    @KafkaListener(topics = "compatable", groupId = "compatable-group")
    public void matcher(KafkaUserInput input) {
        try {
            System.out.println("Consumed from compatable");
            System.out.println(input);
            matcher.processMatch(input);
            // Process independently
            // Save to DB / cache / response queue etc.

        } catch (Exception e) {
            System.err.println("Error processing compatable message");
            e.printStackTrace();
        }
    }

}
