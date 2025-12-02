package com.example.dateon.Controllers;

import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Welcome {


    @Autowired
    UserRepo repo;
    @GetMapping("/")
    public String returner(){
        return "Welcome to Dating";
    }

//    @GetMapping("ak")
//    public String checker(){
////        Users u23 = repo.findNearestCompatibleUser("Female",0.85);
////        System.out.println("User Akhil has been matched with" + u23.getName());
////        return (" \uD83D\uDC9E User Akhil has been matched with " + u23.getName());
//    }

}
