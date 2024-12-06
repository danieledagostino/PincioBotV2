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
    private String questionText; // Domanda originale
    private String confirmedAnswer; // Risposta confermata dagli utenti
    private boolean confirmed; // Indica se la risposta è stata confermata
    private List<String> possibleAnswers = new ArrayList<>(); // Lista delle possibili risposte
    private String response; // Risposta generata (se disponibile)
    private boolean isAnswered; // Indica se la domanda ha già ricevuto una risposta
    private List<String> replacedWords = new ArrayList<>(); // Parole sostituite con [MASK]
    private String status; // Stato della domanda (es. "Unprocessed", "Processed")
    private boolean sentToHuggingFace = false; // Indica se la domanda è stata inviata a Hugging Face


    public void addPossibleAnswer(String answer) {
        if (!possibleAnswers.contains(answer)) {
            possibleAnswers.add(answer);
        }
    }

    public void addReplacedWord(String word) {
        if (!replacedWords.contains(word)) {
            replacedWords.add(word);
        }
    }

    public Question() {
    }

    // Costruttore per inizializzare con il solo testo della domanda
    public Question(String questionText) {
        this.questionText = questionText;
        this.confirmed = false;
        this.isAnswered = false;
    }

    // Costruttore per inizializzare con testo e stato della domanda
    public Question(String questionText, String status) {
        this.questionText = questionText;
        this.status = status;
        this.confirmed = false;
        this.isAnswered = false;
    }

    public Question(String id, String questionText, List<String> possibleAnswers, String confirmedAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.possibleAnswers = new ArrayList<>(possibleAnswers);
        this.confirmedAnswer = confirmedAnswer;
    }
}
