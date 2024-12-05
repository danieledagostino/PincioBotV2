
package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import com.pincio.telegramwebhook.service.AIResponseService;
import com.pincio.telegramwebhook.service.QuestionService;
import com.pincio.telegramwebhook.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@Slf4j
public class WebhookController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AIResponseService aiResponseService;

    @Autowired
    private TelegramService telegramService;

    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> update) {
        try {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                return ResponseEntity.ok("No message found.");
            }

            String text = (String) message.get("text");
            Integer userId = (Integer) ((Map<String, Object>) message.get("from")).get("id");
            Map<String, Object> replyToMessage = (Map<String, Object>) message.get("reply_to_message");

            if (replyToMessage != null) {
                // Gestione della risposta associata a una domanda
                String questionText = (String) replyToMessage.get("text");
                Question question = questionRepository.findById(questionText).orElse(null);

                if (question != null) {
                    // Aggiungi la risposta alla domanda in Redis
                    question.addPossibleAnswer(text);
                    questionRepository.save(question);
                }
            }

            if (text != null && aiResponseService.isQuestionUsingML(text)) {
                // Controlla se esiste già una risposta
                String response = aiResponseService.getBestResponse(text);

                if (response != null) {
                    telegramService.sendMessage(userId, response);
                } else {
                    // Salva la domanda in Redis
                    Question newQuestion = new Question();
                    newQuestion.setQuestionText(text);
                    questionRepository.save(newQuestion);
                }

                return ResponseEntity.ok("Question processed.");
            }

            return ResponseEntity.ok("Message received but not relevant.");
        } catch (Exception e) {
            log.error("Error in receiveMessage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing message.");
        }
    }


    // Metodo per estrarre il testo del messaggio dal JSON
    private String extractMessageText(String jsonPayload) {
        // Implementa la logica per estrarre il testo del messaggio dal JSON del webhook
        // Utilizza una libreria JSON (come org.json o Jackson) per parsare il JSON
        return "Quando ci sono le lezioni?";  // esempio
    }

}
