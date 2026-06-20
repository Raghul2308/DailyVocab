package com.projectAura.DailyVocab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class VocabSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(VocabSchedulerService.class);

    private final EmailService emailService;

    public VocabSchedulerService(EmailService emailService) {
        this.emailService = emailService;
    }

    // Runs every day at 1:00 PM IST (7:30 AM UTC)
    @Scheduled(cron = "${vocab.schedule.cron}", zone = "UTC")
    public void sendDailyVocabEmail() {
        log.info("Scheduler triggered: sending daily vocab email...");
        try {
            emailService.sendDailyVocab();
            log.info("Daily vocab email sent successfully.");
        } catch (Exception e) {
            log.error("Scheduler failed to send daily vocab email: {}", e.getMessage(), e);
        }
    }
}
