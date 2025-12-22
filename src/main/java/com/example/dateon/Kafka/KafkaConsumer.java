package com.example.dateon.Kafka;

import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import com.example.dateon.Service.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    Matcher matcher;
    @Autowired
    private KafkaProducer kafkaProducer;

    // LISTENER 1: Consume Free_user
    @KafkaListener(
            topics = "Free_user",
            groupId = "free-user-group"
    )
    public void firstai(Users user) {
        try {
            System.out.println("Consumed from Free_user");
            System.out.println(user);

            KafkaUserInput input = new KafkaUserInput();
            input.setId(user.getId());
            input.setScore(user.getCompatibilityScore());
            input.setGender(user.getGender());
            input.setLock(true);

            kafkaProducer.checker(input);

        } catch (Exception e) {
            System.err.println("Error processing Free_user message");
            e.printStackTrace();
        }
    }

    // LISTENER 2: Consume compatable
    @KafkaListener(
            topics = "compatable",
            groupId = "compatable-group"
    )
    public void matcher(KafkaUserInput input) {
        try {
            System.out.println("Consumed from compatable");
            System.out.println(input);
            matcher.processMatch(input);
            // Process independently
            // Save to DB / cache / response queue etc.

        } catch (Exception e) {
            System.err.println("Error processing compatable message");
            e.printStackTrace();
        }
    }


}
