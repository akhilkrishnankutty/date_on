package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaProducer;
import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    private KafkaProducer kafkaProducer;

    @Transactional
    public void processMatch(KafkaUserInput input) {

        if (!input.isLock()) {
            return;
        }

        Users currentUser = userRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If already matched, don't process again
        if ("MATCHED".equals(currentUser.getStatus())) {
            System.out.println("User " + currentUser.getId() + " is already matched. Skipping.");
            return;
        }

        // If paused, don't process match
        if (currentUser.isPaused()) {
            System.out.println("User " + currentUser.getId() + " is paused. Skipping match process.");
            return;
        } else {
            System.out.println(
                    "Processing match for User " + currentUser.getId() + " (Paused: " + currentUser.isPaused() + ")");
        }

        // Parse past matches into a list of excluded IDs
        List<Integer> excludedIds = getPastMatchIds(currentUser);
        // Also exclude the current user from matches
        excludedIds.add(currentUser.getId());

        List<Users> matches = userRepo.findNearestCompatibleUsers(
                currentUser.getGender(),
                currentUser.getCompatibilityScore(),
                excludedIds);

        if (matches.isEmpty()) {
            System.out.println("No compatible matches found for User ID: " + currentUser.getId());

            // Set status to WAITING_FOR_MATCH so frontend knows
            currentUser.setStatus("WAITING_FOR_MATCH");
            userRepo.save(currentUser);

            // Re-queue for retry after a delay (async)
            scheduleRetry(currentUser);
            return;
        }

        Users matchedUser = matches.get(0);

        // Check if the matched user also hasn't matched with current user before
        List<Integer> matchedUserPastMatches = getPastMatchIds(matchedUser);
        if (matchedUserPastMatches.contains(currentUser.getId())) {
            System.out.println("Matched user already had current user in past matches. Skipping...");
            // Re-queue for retry
            currentUser.setStatus("WAITING_FOR_MATCH");
            userRepo.save(currentUser);
            scheduleRetry(currentUser);
            return;
        }

        System.out.println("Matched with " + matchedUser.getName());

        // Lock both users atomically
        currentUser.setLoid(matchedUser.getId());
        matchedUser.setLoid(currentUser.getId());

        currentUser.setLock(true);
        matchedUser.setLock(true);

        // Update Status and Time
        currentUser.setStatus("MATCHED");
        matchedUser.setStatus("MATCHED");
        currentUser.setMatchTime(java.time.LocalDateTime.now());
        matchedUser.setMatchTime(java.time.LocalDateTime.now());

        // Record past matches for both users
        addToPastMatches(currentUser, matchedUser.getId());
        addToPastMatches(matchedUser, currentUser.getId());

        userRepo.save(currentUser);
        userRepo.save(matchedUser);
        System.out.println("Match persisted for Users: " + currentUser.getId() + " & " + matchedUser.getId());
    }

    /**
     * Schedule a retry by putting user back in Kafka queue after a delay
     */
    @Async
    public void scheduleRetry(Users user) {
        try {
            // Wait 30 seconds before retrying
            Thread.sleep(30000);

            // Re-fetch user to check if they were matched in the meantime
            Users refreshedUser = userRepo.findById(user.getId()).orElse(null);

            // Only retry if they are still waiting for a match
            // Only retry if they are still waiting for a match AND not paused
            if (refreshedUser != null &&
                    ("WAITING_FOR_MATCH".equals(refreshedUser.getStatus())
                            || "MATCH_FINDING".equals(refreshedUser.getStatus()))
                    && !"MATCHED".equals(refreshedUser.getStatus())
                    && !refreshedUser.isPaused()) {

                System.out.println("Re-queuing User ID: " + user.getId() + " for matching retry");

                // Construct KafkaUserInput to send directly to matcher queue
                KafkaUserInput input = new KafkaUserInput();
                input.setId(refreshedUser.getId());
                input.setScore(refreshedUser.getCompatibilityScore());
                input.setGender(refreshedUser.getGender());
                input.setLock(true); // Ensure lock is set to true for processing

                // Send directly to 'compatable' topic, bypassing 'Free_user' which resets
                // status
                kafkaProducer.checker(input);
            } else {
                if (refreshedUser != null && refreshedUser.isPaused()) {
                    System.out.println("User " + user.getId() + " paused. Stopping retry loop.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Retry scheduling interrupted for User ID: " + user.getId());
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
