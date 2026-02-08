package com.example.dateon.Controllers;

import com.example.dateon.Models.IceBreaker;
import com.example.dateon.Repositories.IceBreakerRepository;
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
