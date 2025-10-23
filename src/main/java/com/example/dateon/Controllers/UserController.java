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
    public Users createNewUser(@RequestBody Users u1){
        return userServices.createNewUser(u1);
    }
}
