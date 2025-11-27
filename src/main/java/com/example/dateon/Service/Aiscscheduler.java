package com.example.dateon.Service;

import com.example.dateon.Kafka.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class Aiscscheduler {

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Scheduled(fixedRate = 1000)
    public void firstAiScheduler(){
//        kafkaConsumer.firstai();
    }

}
