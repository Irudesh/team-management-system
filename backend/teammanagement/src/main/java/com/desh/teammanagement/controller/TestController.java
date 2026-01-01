package com.desh.teammanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")

public class TestController {
<<<<<<< HEAD

=======
    
>>>>>>> 5c35fd684414b77498e7c77bcc4339329c62a32d
    @GetMapping("/hello")
    public String hello() {
        return "Spring Boot backend is running successfully!";
    }
}
