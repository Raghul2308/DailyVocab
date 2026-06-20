package com.projectAura.DailyVocab.service;

import com.projectAura.DailyVocab.model.VocabWord;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.List;

@Service
public class EmailService {

    private final GeminiVocabService vocabService;
    private final TemplateEngine templateEngine;
    private final Resend resend;

    @Value("${mail.recipient}")
    private String[] recipients;

    @Value("${mail.from}")
    private String sender;

    public EmailService(GeminiVocabService vocabService, TemplateEngine templateEngine,
                        @Value("${resend.api.key}") String resendApiKey) {
        this.vocabService = vocabService;
        this.templateEngine = templateEngine;
        this.resend = new Resend(resendApiKey);
    }

    public void sendHelloWorld() throws Exception {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(sender)
                .to(Arrays.asList(recipients))
                .subject("Hello World!")
                .html("<p>Hello World! This is a test email from DailyVocab.</p>")
                .build();
        resend.emails().send(params);
    }

    public List<VocabWord> sendDailyVocab() throws Exception {
        List<VocabWord> words = vocabService.generateVocabWords();

        Context context = new Context();
        context.setVariable("words", words);
        String html = templateEngine.process("vocab-email", context);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(sender)
                .to(Arrays.asList(recipients))
                .subject("Your Daily Vocabulary - 5 New Words!")
                .html(html)
                .build();
        resend.emails().send(params);

        return words;
    }
}

