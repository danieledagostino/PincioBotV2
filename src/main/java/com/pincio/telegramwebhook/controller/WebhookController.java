
package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public void handleWebhook(@RequestBody Map<String, Object> update) {
        questionService.processUpdate(update);
    }

    @PostMapping("/confirm")
    public void confirmResponse(@RequestParam String questionId, @RequestParam String answer) {
        questionService.confirmResponse(questionId, answer);
    }
}
