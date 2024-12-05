package com.pincio.telegramwebhook.model;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@RedisHash("Question")
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String questionText;
    private String confirmedAnswer;
    private boolean confirmed;
    private List<String> possibleAnswers = new ArrayList<>();
    private String response;
    private boolean isAnswered;

    public void addPossibleAnswer(String answer) {
        if (!possibleAnswers.contains(answer)) {
            possibleAnswers.add(answer);
        }
    }

    public Question() {
    }

    public Question(String id, String questionText, List<String> possibleAnswers, String confirmedAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.possibleAnswers = new ArrayList<>();
        this.confirmedAnswer = confirmedAnswer;
    }
}
