
package com.pincio.telegramwebhook.service;

import com.pincio.telegramwebhook.exception.MaskTokenNotFoundException;
import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class AIResponseService {

    @Autowired
    private QuestionRepository questionRepository;

    @Value("${ngl.token}")
    private String huggingFaceToken;

    @Autowired
    private RestTemplate restTemplate;

    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/bert-base-uncased";

    public String suggestResponse(String question, Map<String, String> confirmedQA) {
        double[] questionEmbedding = computeEmbedding(question);
        String bestMatch = null;
        double bestSimilarity = -1;

        for (Map.Entry<String, String> entry : confirmedQA.entrySet()) {
            double[] confirmedEmbedding = computeEmbedding(entry.getKey());
            double similarity = cosineSimilarity(questionEmbedding, confirmedEmbedding);

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = entry.getValue();
            }
        }

        return bestMatch;
    }

    public double calculateSimilarity(String question, String confirmedQuestion) {
        double[] questionEmbedding = computeEmbedding(question);
        double[] confirmedEmbedding = computeEmbedding(confirmedQuestion);
        return cosineSimilarity(questionEmbedding, confirmedEmbedding);
    }

    private double[] computeEmbedding(String text) {
        return text.toLowerCase().chars().mapToDouble(c -> c).toArray();
    }

    public double cosineSimilarity(double[] vectorA, double[] vectorB) {
        int length = Math.min(vectorA.length, vectorB.length);

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double cosineSimilarity(String text1, String text2) {
        Map<String, Integer> vector1 = getVector(text1);
        Map<String, Integer> vector2 = getVector(text2);

        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            int count1 = vector1.getOrDefault(word, 0);
            int count2 = vector2.getOrDefault(word, 0);

            dotProduct += count1 * count2;
            norm1 += Math.pow(count1, 2);
            norm2 += Math.pow(count2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> getVector(String text) {
        Map<String, Integer> vector = new HashMap<>();
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            vector.put(word, vector.getOrDefault(word, 0) + 1);
        }
        return vector;
    }

    public boolean isQuestionUsingML(String message) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            // Esegui la chiamata POST
            String response = callHuggingFaceApiWithMask(message);

            log.info("Response from model: {}", response);
            JSONObject jsonResponse = new JSONObject(response);
            // Estrai la predizione dal JSON, per esempio un valore che indica se è una domanda
            double score = jsonResponse.getJSONArray("scores").getDouble(0);
            return score > 0.5; // Se la probabilità che sia una domanda è > 50%

        } catch (Exception e) {
            log.error("Error while calling model", e);
            return false;
        }
    }

    public String callHuggingFaceApiWithMask(String prompt) {
        String url = "https://api-inference.huggingface.co/models/bert-base-uncased";

        // Aggiungi il token [MASK] se non presente
        if (!prompt.contains("[MASK]")) {
            try {
                // Se non c'è il token [MASK], lancia un'eccezione e salva la domanda in Redis
                throw new MaskTokenNotFoundException("Token [MASK] mancante nella domanda.");
            } catch (MaskTokenNotFoundException e) {
                Question question = new Question();
                question.setQuestionText(prompt);
                question.setAnswered(false);  // La domanda non è ancora stata risposta
                questionRepository.save(question);
                return "La domanda è stata salvata per la revisione.";
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceToken);  // Usa il tuo token di accesso Hugging Face

        String body = "{\"inputs\": \"" + prompt + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Errore API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata a Hugging Face", e);
        }
    }

    public String getBestResponse(String questionText) {
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            return null;
        }

        double similarityThreshold = 0.7;
        Question bestMatch = null;
        double bestSimilarity = -1;

        for (Question question : questions) {
            double similarity = cosineSimilarity(questionText, question.getQuestionText());
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = question;
            }
        }

        if (bestSimilarity >= similarityThreshold && bestMatch != null) {
            return bestMatch.getConfirmedAnswer(); // Usa la risposta confermata
        }

        return null; // Nessuna risposta soddisfacente
    }

}
