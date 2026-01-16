package com.example.dateon.Controllers;

import com.example.dateon.Models.Users;
import com.example.dateon.Service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
