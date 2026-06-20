package com.projectAura.DailyVocab.controller;

import com.projectAura.DailyVocab.model.VocabWord;
import com.projectAura.DailyVocab.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/hello")
    public String sendHelloWorldEmail() throws Exception {
        emailService.sendHelloWorld();
        return "Email sent successfully";
    }

    @GetMapping("/daily-vocab")
    public List<VocabWord> sendDailyVocab() throws Exception {
        return emailService.sendDailyVocab();
    }
}
