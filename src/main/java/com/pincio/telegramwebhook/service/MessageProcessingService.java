package com.pincio.telegramwebhook.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessingService {
    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/bert-base-uncased";

    public boolean isQuestionUsingML(String message) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(MODEL_URL);
            post.setHeader("Authorization", "Bearer <YOUR_HUGGING_FACE_API_KEY>");
            post.setEntity(new org.apache.http.entity.StringEntity("{\"inputs\": \"" + message + "\"}"));

            HttpEntity entity = client.execute(post).getEntity();
            String response = EntityUtils.toString(entity);

            JSONObject jsonResponse = new JSONObject(response);
            // Estrai la predizione dal JSON, per esempio un valore che indica se è una domanda
            double score = jsonResponse.getJSONArray("scores").getDouble(0);
            return score > 0.5; // Se la probabilità che sia una domanda è > 50%
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Metodo che integra la logica ML con un controllo regex per le domande
    public boolean isQuestion(String message) {
        // Prima verifica con regex
        if (isSimpleQuestion(message)) {
            return true;
        }

        // Poi usa il modello ML se non è chiaro
        return isQuestionUsingML(message);
    }

    // Metodo di controllo delle domande con regex
    private boolean isSimpleQuestion(String message) {
        String[] questionKeywords = {"come", "dove", "quando", "perché", "cosa"};

        // Verifica se il messaggio contiene una parola tipica di una domanda
        for (String keyword : questionKeywords) {
            if (message.toLowerCase().contains(keyword)) {
                return true;
            }
        }

        // Verifica se il messaggio termina con un punto interrogativo
        return message.endsWith("?");
    }
}
