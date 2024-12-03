
package com.pincio.telegramwebhook.model;

import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

@RedisHash("Question")
public class Question implements Serializable {
    private String id;
    private String questionText;
    private List<String> answers;
    private String confirmedAnswer;

    public Question(String id, String questionText, List<String> answers, String confirmedAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.answers = answers;
        this.confirmedAnswer = confirmedAnswer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public String getConfirmedAnswer() {
        return confirmedAnswer;
    }

    public void setConfirmedAnswer(String confirmedAnswer) {
        this.confirmedAnswer = confirmedAnswer;
    }
}
