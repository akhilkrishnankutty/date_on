package com.example.dateon.Service;

import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Matcher {

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public void processMatch(KafkaUserInput input) {

        if (!input.isLock()) {
            return;
        }

        Users currentUser = userRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Users> matches = userRepo.findNearestCompatibleUsers(
                currentUser.getGender(),
                currentUser.getCompatibilityScore()
        );

        if (matches.isEmpty()) {
            return;
        }

        Users matchedUser = matches.get(0);
        System.out.println("Matched with"+matches.get(0));
        // Lock both users atomically
        currentUser.setLoid(matchedUser.getId());
        matchedUser.setLoid(currentUser.getId());

        currentUser.setLock(true);
        matchedUser.setLock(true);

        userRepo.save(currentUser);
        userRepo.save(matchedUser);
        System.out.println();
    }
}
