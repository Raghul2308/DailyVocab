package com.projectAura.DailyVocab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/quiz")
public class QuizController {

    @GetMapping("/check")
    @ResponseBody
    public String checkAnswer(
            @RequestParam String word,
            @RequestParam String chosen,
            @RequestParam String correct,
            @RequestParam String meaning) {

        boolean isCorrect = chosen.equalsIgnoreCase(correct);

        String color = isCorrect ? "#2e7d32" : "#c62828";
        String icon = isCorrect ? "✅" : "❌";
        String title = isCorrect ? "Correct!" : "Not quite!";
        String message = isCorrect
                ? "Great job! <strong>" + escapeHtml(word) + "</strong> means: " + escapeHtml(meaning)
                : "The correct answer for <strong>" + escapeHtml(word) + "</strong> is: " + escapeHtml(meaning);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Quiz Result</title>
                  <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: Arial, sans-serif; background: #f0f2f5;
                           display: flex; justify-content: center; align-items: center;
                           min-height: 100vh; padding: 20px; }
                    .card { background: white; border-radius: 16px; padding: 48px 40px;
                            max-width: 460px; width: 100%%;
                            box-shadow: 0 8px 32px rgba(0,0,0,0.12); text-align: center; }
                    .icon { font-size: 72px; margin-bottom: 20px; }
                    .title { font-size: 32px; font-weight: bold; color: %s; margin-bottom: 12px; }
                    .word { font-size: 13px; color: #999; text-transform: uppercase;
                            letter-spacing: 1px; margin-bottom: 8px; }
                    .meaning { font-size: 17px; color: #333; line-height: 1.6;
                               background: #f8f8f8; border-radius: 10px; padding: 14px 18px;
                               margin-top: 12px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="icon">%s</div>
                    <div class="title">%s</div>
                    <div class="word">%s</div>
                    <div class="meaning">%s</div>
                  </div>
                </body>
                </html>
                """.formatted(color, icon, title, escapeHtml(word), escapeHtml(meaning));
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
