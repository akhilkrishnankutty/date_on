package com.example.dateon.Kafka;

import com.example.dateon.Models.KafkaUserInput;
import com.example.dateon.Models.Users;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void available(Users kui){
        kafkaTemplate.send("Free_user",kui);
    }

    public void checker(KafkaUserInput ku2){
        kafkaTemplate.send("compatable",ku2);
    }


}