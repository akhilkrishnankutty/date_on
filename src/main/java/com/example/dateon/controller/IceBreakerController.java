package com.example.dateon.controller;

import com.example.dateon.model.IceBreaker;
import com.example.dateon.repository.IceBreakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/icebreakers")
public class IceBreakerController {

    @Autowired
    private IceBreakerRepository iceBreakerRepository;

    @GetMapping("/random")
    public List<IceBreaker> getRandomIceBreakers(@RequestParam(defaultValue = "3") int limit) {
        return iceBreakerRepository.findRandomIceBreakers(limit);
    }
}
