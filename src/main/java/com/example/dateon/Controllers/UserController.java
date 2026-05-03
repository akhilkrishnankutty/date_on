package com.example.dateon.Controllers;

import com.example.dateon.Models.Users;
import com.example.dateon.Service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    UserServices userServices;

    @PostMapping("/create")
    public Users createNewUser(@RequestBody Users u1) {
        return userServices.createNewUser(u1);
    }

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Autowired
    private com.example.dateon.Service.JwtService jwtService;

    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<?> login(@RequestBody Users user) {
        org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.getMail(),
                        user.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(user.getMail());
            Users loggedInUser = userServices.getUserByMail(user.getMail());
            // Return a Map or a DTO with token and user details
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("token", token);
            response.put("user", loggedInUser);
            return org.springframework.http.ResponseEntity.ok(response);
        } else {
            return org.springframework.http.ResponseEntity.status(401).body("Invalid Credentials");
        }
    }

    @PostMapping("/{userId}/complete")
    public org.springframework.http.ResponseEntity<?> complete(
            @org.springframework.web.bind.annotation.PathVariable int userId) {
        try {
            return org.springframework.http.ResponseEntity.ok(userServices.completeProfile(userId));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> getUser(
            @org.springframework.web.bind.annotation.PathVariable int id,
            org.springframework.security.core.Authentication authentication) {
        Users user = userServices.getUserById(id);
        if (user == null)
            return org.springframework.http.ResponseEntity.status(404).body("User not found");

        String currentUsername = authentication.getName();
        Users currentUser = userServices.getUserByMail(currentUsername);

        if (currentUser == null) {
            return org.springframework.http.ResponseEntity.status(401).body("Unauthorized");
        }

        if (currentUser.getId() == id) {
            // Requesting own profile, mask password before returning
            user.setPassword("");
            return org.springframework.http.ResponseEntity.ok(user);
        }

        if ("MATCHED".equals(currentUser.getStatus()) && currentUser.getLoid() == user.getId()) {
            // Return full profile but mask sensitive contact info
            Users safeUser = new Users();
            // Copy all public fields
            safeUser.setId(user.getId());
            safeUser.setName(user.getName());
            safeUser.setBio(user.getBio());

            // SECURITY FIX: Do not rely on frontend to blur image.
            // Only send image URL if 5 days have passed since match time.
            if (currentUser.getMatchTime() != null &&
                java.time.LocalDateTime.now().isAfter(currentUser.getMatchTime().plusDays(5))) {
                safeUser.setProfilePictureUrl(user.getProfilePictureUrl());
            } else {
                safeUser.setProfilePictureUrl("");
            }

            safeUser.setAge(user.getAge());
            safeUser.setGender(user.getGender());
            safeUser.setWorkplace(user.getWorkplace());
            safeUser.setInterests(user.getInterests());
            safeUser.setLocation(user.getLocation());
            safeUser.setStatus(user.getStatus());
            safeUser.setLoid(user.getLoid());
            safeUser.setMatchTime(user.getMatchTime());

            // HIDE SENSITIVE FIELDS
            safeUser.setMail(""); // Hide email
            safeUser.setNumber(0); // Hide phone
            safeUser.setPassword(""); // Ensure password is empty (should be anyway)
            safeUser.setLock(user.isLock());

            // SHOW CUSTOM QUESTION & ANSWER
            safeUser.setCustomQuestion(user.getCustomQuestion());
            safeUser.setAnswerToMatchQuestion(user.getAnswerToMatchQuestion());

            return org.springframework.http.ResponseEntity.ok(safeUser);
        }
        return org.springframework.http.ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping("/{userId}/unmatch")
    public org.springframework.http.ResponseEntity<?> unmatch(
            @org.springframework.web.bind.annotation.PathVariable int userId) {
        try {
            return org.springframework.http.ResponseEntity.ok(userServices.unmatchUser(userId));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/unlock-cooldown")
    public org.springframework.http.ResponseEntity<?> unlockCooldown(
            @org.springframework.web.bind.annotation.PathVariable int userId) {
        try {
            userServices.unlockCooldown(userId);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Cooldown unlocked"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/update")
    public org.springframework.http.ResponseEntity<?> updateUser(
            @org.springframework.web.bind.annotation.PathVariable int id,
            @RequestBody Users user) {
        try {
            return org.springframework.http.ResponseEntity.ok(userServices.updateUser(id, user));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@org.springframework.web.bind.annotation.PathVariable int id,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(userServices.uploadProfilePicture(id, file));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/pause")
    public ResponseEntity<?> togglePause(@org.springframework.web.bind.annotation.PathVariable int userId) {
        try {
            boolean isPaused = userServices.toggleAccountPause(userId);
            java.util.Map<String, Boolean> response = new java.util.HashMap<>();
            response.put("isPaused", isPaused);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/answer-match-question")
    public ResponseEntity<?> answerMatchQuestion(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String answer = payload.get("answer");
            if (answer == null || answer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Answer cannot be empty");
            }
            return ResponseEntity.ok(userServices.answerMatchQuestion(userId, answer));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/custom-question")
    public ResponseEntity<?> saveCustomQuestion(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String question = payload.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Question cannot be empty");
            }
            return ResponseEntity.ok(userServices.saveCustomQuestion(userId, question));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> payload) {
        try {
            String email = payload.get("mail");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email cannot be empty");
            }
            userServices.processForgotPassword(email);
            return ResponseEntity.ok(
                    java.util.Map.of("message", "Temporary password generated. It will take up to 48hrs to verify."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String oldPassword = payload.get("oldPassword");
            String newPassword = payload.get("newPassword");
            userServices.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(java.util.Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<?> deleteUser(@org.springframework.web.bind.annotation.PathVariable int userId) {
        try {
            userServices.deleteUser(userId);
            return ResponseEntity.ok(java.util.Map.of("message", "User account deleted permanently"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            @RequestBody java.util.Map<String, String> payload) {
        try {
            String token = payload.get("token");
            userServices.updateFcmToken(userId, token);
            return ResponseEntity.ok(java.util.Map.of("message", "FCM Token updated"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
