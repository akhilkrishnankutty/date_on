package com.example.dateon.Kafka;

import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @Autowired
    KafkaProducer kpa;

    @Getter
    KafkaUserInput kui = new KafkaUserInput();
//    @Scheduled(fixedRate = 200)
    @KafkaListener(topics = "Free_user",groupId = "user-ai-group")
    public void firstai(Users u1){
//        Math.random();
        System.out.println("ran");
        System.out.println(u1.toString());
        KafkaUserInput kui = new KafkaUserInput();
        kui.setLock(u1.isLock());
        kui.setId(u1.getId());
        kui.setScore(u1.getCompatibilityScore());
        kui.setGender(u1.getGender());
        kui.setLock(true);
        kpa.checker(kui);

    }
    @KafkaListener(topics = "compatable",groupId = "user-ai-group")
    public void matcher(KafkaUserInput ku1){
        kui.setId(ku1.getId());
        kui.setScore(ku1.getScore());
        kui.setLock(ku1.isLock());

    }

}
