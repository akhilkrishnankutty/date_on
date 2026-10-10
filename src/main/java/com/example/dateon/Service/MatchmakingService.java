package com.example.dateon.Service;

import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchmakingService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AIService aiService;

    @Autowired
    private Matcher matcher;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void broadcastUserUpdate(int userId) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/user.status." + userId, "UPDATE");
        }
    }

    /**
     * Asynchronously triggers candidate search, AI scoring, and matching.
     * Replaces the Kafka Free_user -> AI -> compatable pipeline with in-process async.
     */
    @Async("matchmakingExecutor")
    public void processUserMatchmakingAsync(int userId) {
        try {
            Users user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return;
            }

            if (user.isPaused()) {
                System.out.println("User " + user.getName() + " is paused. Skipping matchmaking.");
                return;
            }

            if (user.isLock() || "COOLDOWN".equals(user.getStatus())) {
                System.out.println("User " + user.getName() + " is locked or in cooldown. Skipping matchmaking.");
                return;
            }

            if ("MATCHED".equals(user.getStatus())) {
                System.out.println("User " + user.getName() + " is already matched. Skipping.");
                return;
            }

            System.out.println("Starting Async Matchmaking for: " + user.getName());

            // Build list of excluded IDs (self + past matches)
            List<Integer> excludedIds = getPastMatchIds(user);
            if (!excludedIds.contains(user.getId())) {
                excludedIds.add(user.getId());
            }

            // 1. Cascading candidate search with fallback
            List<Users> candidates = findCandidatesWithFallback(user, excludedIds);

            double maxScore = 0.0;
            if (!candidates.isEmpty()) {
                System.out.println("Found " + candidates.size() + " optimal candidates for " + user.getName() + ". Calling AI Service...");
                maxScore = aiService.getCompatibilityScore(user, candidates);
            } else {
                System.out.println("No candidates available for " + user.getName() + " across all tiers. Setting to WAITING_FOR_MATCH.");
            }

            // Refresh user state before updating
            Users freshUser = userRepo.findById(userId).orElse(user);
            if (freshUser.isPaused() || freshUser.isLock() || "COOLDOWN".equals(freshUser.getStatus()) || "MATCHED".equals(freshUser.getStatus())) {
                return;
            }

            freshUser.setCompatibilityScore(maxScore);
            freshUser.setStatus("MATCH_FINDING");
            userRepo.save(freshUser);
            broadcastUserUpdate(freshUser.getId());

            System.out.println("AI Processing Complete for User ID: " + freshUser.getId() + ", Max Score: " + maxScore);

            // 2. Trigger match pairing
            matcher.processMatch(freshUser.getId());

        } catch (Exception e) {
            System.err.println("Error in processUserMatchmakingAsync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cascading search: Tier 1 (Optimal local) -> Tier 2 (Broad/Nationwide) -> Tier 3 (Empty)
     */
    public List<Users> findCandidatesWithFallback(Users user, List<Integer> excludedIds) {
        Pageable top25 = PageRequest.of(0, 25);
        String userLocation = user.getLocation() != null ? user.getLocation().trim() : "";

        // Tier 1: Optimal (Same city, closest age, active, quiz completed)
        List<Users> rawCandidates = userRepo.findOptimalCandidates(
                user.getGender(),
                userLocation,
                user.getAge(),
                excludedIds,
                top25
        );

        // Ensure candidates have completed quiz answers
        List<Users> candidates = rawCandidates.stream()
                .filter(c -> c.getQuestions() != null && !c.getQuestions().isEmpty())
                .collect(Collectors.toList());

        // Tier 2: Fallback (Broad / Any city, closest age, active, quiz completed)
        if (candidates.isEmpty()) {
            System.out.println("Tier 1 returned 0 candidates. Falling back to Tier 2 (Broad)...");
            List<Users> broadCandidates = userRepo.findBroadCandidates(
                    user.getGender(),
                    user.getAge(),
                    excludedIds,
                    top25
            );
            candidates = broadCandidates.stream()
                    .filter(c -> c.getQuestions() != null && !c.getQuestions().isEmpty())
                    .collect(Collectors.toList());
        }

        return candidates;
    }

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
}
