package com.pincio.telegramwebhook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // Import necessario
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class TelegramWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHandleUpdate() throws Exception {
        String updateJson = "{\"update_id\":12345,\"message\":{\"chat\":{\"id\":67890},\"text\":\"Domanda di test\"}}";

        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}

