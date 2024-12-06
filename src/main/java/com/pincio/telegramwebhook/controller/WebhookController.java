
package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import com.pincio.telegramwebhook.service.AIResponseService;
import com.pincio.telegramwebhook.service.QuestionService;
import com.pincio.telegramwebhook.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void receiveMessage(@RequestBody Map<String, Object> update) {
        try {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                log.info("No message found.");
            }

            String text = (String) message.get("text");
            Integer userId = (Integer) ((Map<String, Object>) message.get("from")).get("id");
            Map<String, Object> replyToMessage = (Map<String, Object>) message.get("reply_to_message");

            if (replyToMessage != null) {
                // Gestione della risposta associata a una domanda
                String questionText = (String) replyToMessage.get("text");
                Question question = questionRepository.findByQuestionText(questionText);

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
                    // check se Question esiste già in Redis
                    Question question = questionRepository.findByQuestionText(text);
                    if (question == null) {
                        questionService.saveQuestion(text);
                    }
                }

                log.info("Question processed.");
            }

            log.info("Message received but not relevant.");
        } catch (Exception e) {
            log.error("Error in receiveMessage: {}", e.getMessage(), e);
            log.error("Error processing message.");
        }
    }


    // Metodo per estrarre il testo del messaggio dal JSON
    private String extractMessageText(String jsonPayload) {
        // Implementa la logica per estrarre il testo del messaggio dal JSON del webhook
        // Utilizza una libreria JSON (come org.json o Jackson) per parsare il JSON
        return "Quando ci sono le lezioni?";  // esempio
    }

}
