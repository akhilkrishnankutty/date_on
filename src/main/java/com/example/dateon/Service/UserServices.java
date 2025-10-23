package com.example.dateon.Service;

import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServices {

    @Autowired
    UserRepo repo;


    public Users createNewUser(Users u1) {
        return repo.save(u1);
    }
}
