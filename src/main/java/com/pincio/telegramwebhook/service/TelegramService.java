package com.pincio.telegramwebhook.service;

import com.pincio.telegramwebhook.config.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramService {

    @Autowired
    private TelegramBotConfig botConfig;

    public void sendMessage(Integer userId, String messageText) {
        String apiUrl = "https://api.telegram.org/bot" + botConfig.getBotToken() + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", userId);
        body.put("text", messageText);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(apiUrl, body, String.class);
        } catch (Exception e) {
            log.error("Error sending message to user {}: {}", userId, e.getMessage(), e);
        }
    }

}
