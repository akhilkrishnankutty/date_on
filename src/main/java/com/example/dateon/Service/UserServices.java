package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaProducer;
import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServices {

    @Autowired
    UserRepo repo;
    @Autowired
    KafkaProducer kafkaProducer;


    public Users createNewUser(@org.jetbrains.annotations.NotNull Users u1) {
        u1.setLock(true);
        repo.save(u1);
        kafkaProducer.available(u1);
        return u1;
    }
}
