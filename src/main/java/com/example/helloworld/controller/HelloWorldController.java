package com.example.helloworld.controller;

import com.example.helloworld.dto.GreetingResponse;
import com.example.helloworld.exception.InvalidInputException;
import com.example.helloworld.service.GreetingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    private static final String NAME = "name";

    private final GreetingService greetingService;

    public HelloWorldController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello-world")
    public GreetingResponse helloWorld(@RequestParam(NAME) String name, HttpServletRequest request) {
        // Only one name is allowed
        if (request.getParameterValues(NAME).length > 1) {
            throw new InvalidInputException("Name must be given exactly once");
        }
        return new GreetingResponse(greetingService.greet(name));
    }
}
