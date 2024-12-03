
package com.pincio.telegramwebhook.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIResponseService {

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

    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
