
package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.service.QuestionService;
import com.pincio.telegramwebhook.service.MessageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private MessageProcessingService messageProcessingService;

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public void receiveMessage(@RequestBody String jsonPayload) {
        // Parse JSON and extract message text (questo dipende dal formato effettivo del payload)
        String userMessage = extractMessageText(jsonPayload);

        // Verifica se il messaggio è una domanda
        boolean isQuestion = messageProcessingService.isQuestion(userMessage);

        if (isQuestion) {
            // Logica per trattare la domanda (es. salva in Redis, etc.)
            questionService.saveQuestion(userMessage);
        } else {
            // Logica per trattare un messaggio che non è una domanda
            System.out.println("Messaggio non identificato come domanda: " + userMessage);
        }
    }

    // Metodo per estrarre il testo del messaggio dal JSON
    private String extractMessageText(String jsonPayload) {
        // Implementa la logica per estrarre il testo del messaggio dal JSON del webhook
        // Utilizza una libreria JSON (come org.json o Jackson) per parsare il JSON
        return "Quando ci sono le lezioni?";  // esempio
    }
}
