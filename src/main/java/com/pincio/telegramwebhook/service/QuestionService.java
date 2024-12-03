
package com.pincio.telegramwebhook.service;

import com.pincio.telegramwebhook.config.TelegramBotConfig;
import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AIResponseService aiResponseService;

    @Autowired
    private TelegramBotConfig botConfig;

    public void processUpdate(Map<String, Object> update) {
        String questionText = extractQuestion(update);
        Optional<Question> existingQuestion = findSimilarQuestion(questionText);

        if (existingQuestion.isPresent()) {
            String suggestedResponse = aiResponseService.suggestResponse(
                questionText, Map.of(existingQuestion.get().getQuestionText(), existingQuestion.get().getConfirmedAnswer())
            );
            respondToUser(update, suggestedResponse);
        } else {
            Question question = new Question(UUID.randomUUID().toString(), questionText, new ArrayList<>(), null);
            questionRepository.save(question);
            notifyAdmin(questionText);
        }
    }

    private String extractQuestion(Map<String, Object> update) {
        return (String) ((Map<String, Object>) update.get("message")).get("text");
    }

    private Optional<Question> findSimilarQuestion(String questionText) {
        List<Question> allQuestions = new ArrayList<>();
        questionRepository.findAll().forEach(allQuestions::add);
        return allQuestions.stream()
                .filter(q -> aiResponseService.calculateSimilarity(questionText, q.getQuestionText()) > 0.8)
                .findFirst();
    }

    private void notifyAdmin(String questionText) {
        // TODO: Add logic to notify admin about a new question
    }

    public void confirmResponse(String questionId, String answer) {
        // Recupera la domanda dal database
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Domanda non trovata con ID: " + questionId));

        // Aggiorna la risposta confermata
        question.setConfirmedAnswer(answer);
        questionRepository.save(question);
    }

    public void respondToUser(Map<String, Object> update, String response) {
        String chatId = extractChatId(update);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            MyTelegramBot bot = new MyTelegramBot(botConfig.getBotToken(), botConfig.getBotUsername());

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(response);

            bot.execute(message); // Invia il messaggio
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Gestione dell'errore
        }
    }

    private String extractChatId(Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        return String.valueOf(message.get("chat_id"));
    }
}
