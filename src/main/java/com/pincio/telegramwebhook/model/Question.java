package com.pincio.telegramwebhook.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@Data
@RedisHash("Question")
public class Question {
    @Id
    private String id;
    private String questionText;
    private List<String> possibleAnswers; // Lista delle risposte proposte
    private String confirmedAnswer; // La risposta confermata
    private Boolean confirmed; // Indica se la risposta è stata confermata

    public Question() {
    }

    public Question(String id, String questionText, List<String> possibleAnswers, String confirmedAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.possibleAnswers = new ArrayList<>();
        this.confirmedAnswer = confirmedAnswer;
    }
}
