package com.example.dateon.Service;

import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Matcher {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AIService aiService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private void broadcastUserUpdate(int userId) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/user.status." + userId, "UPDATE");
        }
    }

    @Transactional
    public void processMatch(int userId) {
        Users currentUser = userRepo.findById(userId).orElse(null);
        if (currentUser == null) {
            return;
        }

        // If already matched, don't process again
        if ("MATCHED".equals(currentUser.getStatus())) {
            System.out.println("User " + currentUser.getId() + " is already matched. Skipping.");
            return;
        }

        // If paused, don't process match
        if (currentUser.isPaused()) {
            System.out.println("User " + currentUser.getId() + " is paused. Skipping match process.");
            return;
        }

        // If locked or in cooldown, don't process match
        if (currentUser.isLock() || "COOLDOWN".equals(currentUser.getStatus())) {
            System.out.println("User " + currentUser.getId() + " is locked or in cooldown. Skipping match process.");
            return;
        }

        // Parse past matches into a list of excluded IDs
        List<Integer> excludedIds = getPastMatchIds(currentUser);
        if (!excludedIds.contains(currentUser.getId())) {
            excludedIds.add(currentUser.getId());
        }

        List<Users> matches = userRepo.findNearestCompatibleUsers(
                currentUser.getGender(),
                currentUser.getCompatibilityScore(),
                excludedIds);

        if (matches.isEmpty()) {
            System.out.println("No compatible matches found for User ID: " + currentUser.getId());

            // Set status to WAITING_FOR_MATCH so frontend knows
            currentUser.setStatus("WAITING_FOR_MATCH");
            userRepo.save(currentUser);
            broadcastUserUpdate(currentUser.getId());
            return;
        }

        Users matchedUser = matches.get(0);

        // Check if the matched user also hasn't matched with current user before
        List<Integer> matchedUserPastMatches = getPastMatchIds(matchedUser);
        if (matchedUserPastMatches.contains(currentUser.getId())) {
            System.out.println("Matched user already had current user in past matches. Skipping...");
            currentUser.setStatus("WAITING_FOR_MATCH");
            userRepo.save(currentUser);
            return;
        }

        System.out.println("Matched " + currentUser.getName() + " with " + matchedUser.getName());

        // Lock both users atomically
        currentUser.setLoid(matchedUser.getId());
        matchedUser.setLoid(currentUser.getId());

        currentUser.setLock(true);
        matchedUser.setLock(true);

        // Update Status and Time
        currentUser.setStatus("MATCHED");
        matchedUser.setStatus("MATCHED");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        currentUser.setMatchTime(now);
        matchedUser.setMatchTime(now);

        // Sync compatibility scores (use the valid one if one is 0)
        double finalScore = Math.max(currentUser.getCompatibilityScore(), matchedUser.getCompatibilityScore());
        
        // If neither user had a pre-computed score, evaluate compatibility dynamically with AI
        if (finalScore <= 0 && aiService != null) {
            try {
                finalScore = aiService.getCompatibilityScore(currentUser, List.of(matchedUser));
            } catch (Exception e) {
                System.err.println("Error evaluating AI compatibility in Matcher: " + e.getMessage());
            }
        }

        String aiMatched = (finalScore > 0) ? "Y" : "N";
        currentUser.setAiMatch(aiMatched);
        matchedUser.setAiMatch(aiMatched);

        if (finalScore > 0) {
            currentUser.setCompatibilityScore(finalScore);
            matchedUser.setCompatibilityScore(finalScore);
        }

        // Record past matches for both users
        addToPastMatches(currentUser, matchedUser.getId());
        addToPastMatches(matchedUser, currentUser.getId());

        userRepo.save(currentUser);
        userRepo.save(matchedUser);
        broadcastUserUpdate(currentUser.getId());
        broadcastUserUpdate(matchedUser.getId());
        System.out.println("Match persisted for Users: " + currentUser.getId() + " & " + matchedUser.getId());
    }

    /**
     * Cron job to process all users waiting for a match every 30 seconds
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void processWaitingUsers() {
        List<Users> waitingUsers = userRepo.findByStatusAndIsPausedFalse("WAITING_FOR_MATCH");
        if (waitingUsers.isEmpty()) {
            return;
        }
        System.out.println("Cron Job: Re-evaluating " + waitingUsers.size() + " waiting users for matching retry");
        for (Users refreshedUser : waitingUsers) {
            processMatch(refreshedUser.getId());
        }
    }


    /**
     * Parse pastMatches string into a list of user IDs
     */
    private List<Integer> getPastMatchIds(Users user) {
        List<Integer> ids = new ArrayList<>();
        if (user.getPastMatches() != null && !user.getPastMatches().isBlank()) {
            ids = Arrays.stream(user.getPastMatches().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return ids;
    }

    /**
     * Add a user ID to the pastMatches field
     */
    private void addToPastMatches(Users user, int matchedUserId) {
        String currentPastMatches = user.getPastMatches();
        if (currentPastMatches == null || currentPastMatches.isBlank()) {
            user.setPastMatches(String.valueOf(matchedUserId));
        } else {
            user.setPastMatches(currentPastMatches + "," + matchedUserId);
        }
    }
}
