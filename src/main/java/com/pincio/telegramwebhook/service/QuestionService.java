
package com.pincio.telegramwebhook.service;

import com.pincio.telegramwebhook.config.TelegramBotConfig;
import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

@Service
@Slf4j
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AIResponseService aiResponseService;

    @Autowired
    private TelegramBotConfig botConfig;

    @Autowired
    private MaskReplacementService maskReplacementService;

    public Optional<Question> getQuestionById(String id) {
        Optional<Question> question = questionRepository.findById(id);
        if (question.isPresent()) {
            return question;
        } else {
            return null;
        }
    }


    public void saveMaskedQuestion(String originalQuestion) {
        String maskedQuestion = maskReplacementService.replaceWithMask(originalQuestion);
        updateReplacementHistory(originalQuestion, maskedQuestion);
        questionRepository.save(new Question(maskedQuestion));
    }

    public void processUpdate(Map<String, Object> update) {
        String questionText = extractQuestion(update);
        Optional<Question> existingQuestion = findSimilarQuestion(questionText);

        if (existingQuestion.isPresent()) {
            try {
                String suggestedResponse = aiResponseService.suggestResponse(
                        questionText,
                        Map.of(
                                existingQuestion.get().getQuestionText(),
                                existingQuestion.get().getConfirmedAnswer() != null ?
                                        existingQuestion.get().getConfirmedAnswer() :
                                        "Nessuna risposta confermata disponibile"
                        )
                );
                respondToUser(update, suggestedResponse);
            } catch (Exception e) {
                log.error("Errore durante la risposta alla domanda", e);
            }
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
        try {
            questionRepository.findAll().forEach(allQuestions::add);
            return allQuestions.stream()
                    .filter(q -> aiResponseService.calculateSimilarity(questionText, q.getQuestionText()) > 0.8)
                    .findFirst();
        } catch (Exception e) {
            log.error("Errore durante il recupero delle domande", e);
            return Optional.empty();
        }
    }

    private void notifyAdmin(String questionText) {
        // TODO: Add logic to notify admin about a new question
    }

    public void confirmResponse(String questionId, String confirmedAnswer) {
        Optional<Question> existingQuestion = questionRepository.findById(questionId);
        if (existingQuestion.isPresent()) {
            Question question = existingQuestion.get();
            question.setConfirmedAnswer(confirmedAnswer); // Imposta la risposta confermata
            questionRepository.save(question); // Salva l'oggetto aggiornato
        } else {
            log.warn("Domanda non trovata con ID: {}", questionId);
        }

        log.debug("Domanda: {}, Risposta confermata: {}",
                existingQuestion.get().getQuestionText(),
                existingQuestion.get().getConfirmedAnswer());

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
            log.error("Errore durante l'invio del messaggio", e);
        }
    }

    private String extractChatId(Map<String, Object> update) {
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        String chatId = null;
        if (message.get("chat") != null)
            chatId = ((Map<String, Object>)message.get("chat")).get("id").toString();
        return chatId;
    }

    public void saveQuestion(String questionText) {
        Question question = new Question();
        question.setQuestionText(questionText);
        question.setConfirmedAnswer(null); // Inizialmente non c'è risposta confermata
        question.setConfirmed(false); // La domanda non è confermata inizialmente

        // Salviamo la domanda nel repository (in Redis o un altro database)
        questionRepository.save(question);
    }   // Creiamo un oggetto Question con il testo della domanda

    public void deleteQuestion(String id) {
        questionRepository.deleteById(id);
    }

    public void updateQuestion(Question question) {
        //question.setConfirmedAnswer(null); // Inizialmente non c'è risposta confermata
        //question.setConfirmed(false); // La domanda non è confermata inizialmente

        // Salviamo la domanda nel repository (in Redis o un altro database)
        questionRepository.save(question);
    }

    // Metodo per aggiornare la risposta di una domanda
    public void updateQuestionResponse(String questionText, String response) {
        Question question = questionRepository.findById(questionText).orElse(null);
        if (question != null) {
            question.setResponse(response);
            question.setAnswered(true);  // La domanda è stata risposta
            questionRepository.save(question);
        }
    }

    // Metodo per ottenere tutte le domande da Redis
    public Iterable<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public void saveUnprocessedQuestion(String questionText) {
        // Logica per salvare la domanda in Redis o in un altro repository
        // In Redis, puoi usare il repository QuestionRepository
        questionRepository.save(new Question(questionText, "Unprocessed"));
    }

    public void updateReplacementHistory(String originalQuestion, String maskedQuestion) {
        String replacedWord = originalQuestion.replace(maskedQuestion.replace("[MASK]", "").trim(), "").trim();
        if (!replacedWord.isEmpty()) {
            maskReplacementService.recordMaskReplacement(replacedWord);
        }
    }

}
