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
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    UserServices userServices;

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Autowired
    private com.example.dateon.Service.JwtService jwtService;

    private ResponseEntity<?> checkAuthorization(int targetUserId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Users currentUser = userServices.getUserByMail(authentication.getName());
        if (currentUser == null || currentUser.getId() != targetUserId) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return null;
    }

    @PostMapping("/create")
    public Users createNewUser(@RequestBody Users u1) {
        return userServices.createNewUser(u1);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.getMail(),
                        user.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(user.getMail());
            Users loggedInUser = userServices.getUserByMail(user.getMail());
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("token", token);
            response.put("user", loggedInUser);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid Credentials");
        }
    }

    @PostMapping("/{userId}/complete")
    public ResponseEntity<?> complete(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

        try {
            return ResponseEntity.ok(userServices.completeProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(
            @org.springframework.web.bind.annotation.PathVariable int id,
            Authentication authentication) {
        Users user = userServices.getUserById(id);
        if (user == null)
            return ResponseEntity.status(404).body("User not found");

        String currentUsername = authentication.getName();
        Users currentUser = userServices.getUserByMail(currentUsername);

        if (currentUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (currentUser.getId() == id) {
            user.setPassword("");
            return ResponseEntity.ok(user);
        }

        if ("MATCHED".equals(currentUser.getStatus()) && currentUser.getLoid() == user.getId()) {
            Users safeUser = new Users();
            safeUser.setId(user.getId());
            safeUser.setName(user.getName());
            safeUser.setBio(user.getBio());
            safeUser.setProfilePictureUrl(user.getProfilePictureUrl());
            safeUser.setAge(user.getAge());
            safeUser.setGender(user.getGender());
            safeUser.setWorkplace(user.getWorkplace());
            safeUser.setInterests(user.getInterests());
            safeUser.setLocation(user.getLocation());
            safeUser.setStatus(user.getStatus());
            safeUser.setLoid(user.getLoid());
            safeUser.setMatchTime(user.getMatchTime());
            safeUser.setMail("");
            safeUser.setNumber(0);
            safeUser.setPassword("");
            safeUser.setLock(user.isLock());
            safeUser.setCustomQuestion(user.getCustomQuestion());
            safeUser.setAnswerToMatchQuestion(user.getAnswerToMatchQuestion());
            return ResponseEntity.ok(safeUser);
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping("/{userId}/unmatch")
    public ResponseEntity<?> unmatch(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

        try {
            return ResponseEntity.ok(userServices.unmatchUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/unlock-cooldown")
    public ResponseEntity<?> unlockCooldown(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

        try {
            userServices.unlockCooldown(userId);
            return ResponseEntity.ok(java.util.Map.of("message", "Cooldown unlocked"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/update")
    public ResponseEntity<?> updateUser(
            @org.springframework.web.bind.annotation.PathVariable int id,
            @RequestBody Users user,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(id, authentication);
        if (authError != null) return authError;

        try {
            return ResponseEntity.ok(userServices.updateUser(id, user));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @org.springframework.web.bind.annotation.PathVariable int id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(id, authentication);
        if (authError != null) return authError;

        try {
            return ResponseEntity.ok(userServices.uploadProfilePicture(id, file));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/pause")
    public ResponseEntity<?> togglePause(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

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
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

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
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

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
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

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
    public ResponseEntity<?> deleteUser(
            @org.springframework.web.bind.annotation.PathVariable int userId,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

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
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        ResponseEntity<?> authError = checkAuthorization(userId, authentication);
        if (authError != null) return authError;

        try {
            String token = payload.get("token");
            userServices.updateFcmToken(userId, token);
            return ResponseEntity.ok(java.util.Map.of("message", "FCM Token updated"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
