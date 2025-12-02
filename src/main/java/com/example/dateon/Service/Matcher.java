package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaConsumer;
import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Repo.UserRepo;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service

public class Matcher {

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Autowired
    UserRepo userRepo;

    @Scheduled(fixedRate = 2000)
    public void getlocked(){
        System.out.println(kafkaConsumer.getKui().toString());
        if (kafkaConsumer.getKui().isLock()){
            System.out.println("i ran");
            KafkaUserInput kafkaUserInput = kafkaConsumer.getKui();
            Users u2 = userRepo.findById(kafkaUserInput.getId()).get();
            Users u1 = userRepo.findNearestCompatibleUsers(u2.getGender(),u2.getCompatibilityScore()).get(0);
            u1.setLoid(kafkaUserInput.getId());
            u2.setLoid(u1.getId());
            u1.setLock(true);
            u2.setLock(true);
            userRepo.save(u1);
            userRepo.save(u2);



        }
    }
}
