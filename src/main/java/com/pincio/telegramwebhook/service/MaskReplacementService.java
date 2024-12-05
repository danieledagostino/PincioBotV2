package com.pincio.telegramwebhook.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MaskReplacementService {

    private final Map<String, Integer> maskHistory = new HashMap<>();

    // Sostituisci automaticamente la parola più comune con [MASK]
    public String replaceWithMask(String questionText) {
        String[] words = questionText.split("\\s+");
        String mostLikelyWord = null;
        int maxFrequency = 0;

        // Trova la parola più probabile da sostituire
        for (String word : words) {
            int frequency = maskHistory.getOrDefault(word.toLowerCase(), 0);
            if (frequency > maxFrequency) {
                mostLikelyWord = word;
                maxFrequency = frequency;
            }
        }

        // Se non c'è una parola candidata, ritorna il testo originale
        if (mostLikelyWord == null) {
            return questionText;
        }

        // Sostituisci la parola con [MASK]
        String maskedQuestion = questionText.replaceFirst("\\b" + mostLikelyWord + "\\b", "[MASK]");
        return maskedQuestion;
    }

    // Aggiorna il conteggio della parola sostituita
    public void recordMaskReplacement(String originalWord) {
        maskHistory.put(originalWord.toLowerCase(), maskHistory.getOrDefault(originalWord.toLowerCase(), 0) + 1);
    }
}

