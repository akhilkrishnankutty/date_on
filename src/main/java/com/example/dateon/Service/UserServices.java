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


    public Users createNewUser(Users u1) {
        u1.setLock(true);
        KafkaUserInput kui = null;
        kui.setId(u1.getId());
        kui.setScore(u1.getCompatibilityScore());
        kafkaProducer.available(kui);
        return repo.save(u1);
    }
}
