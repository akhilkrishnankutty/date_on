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
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer requestorId) {
        Users user = userServices.getUserById(id);
        if (user == null)
            return org.springframework.http.ResponseEntity.status(404).body("User not found");

        if (requestorId != null) {
            Users requestor = userServices.getUserById(requestorId);
            if (requestor != null && "MATCHED".equals(requestor.getStatus()) && requestor.getLoid() == user.getId()) {
                // Return full profile but mask sensitive contact info
                // We do NOT hide name/bio/image URL here. Frontend handles image
                // blurring/locking based on matchTime.
                Users safeUser = new Users();
                // Copy all public fields
                safeUser.setId(user.getId());
                safeUser.setName(user.getName());
                safeUser.setBio(user.getBio());
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

                return org.springframework.http.ResponseEntity.ok(safeUser);
            }
        }
        return org.springframework.http.ResponseEntity.ok(user);
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

    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<?> getProfilePicture(@org.springframework.web.bind.annotation.PathVariable int id) {
        try {
            byte[] image = userServices.getProfilePicture(id);
            String contentType = userServices.getProfilePictureContentType(id);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
