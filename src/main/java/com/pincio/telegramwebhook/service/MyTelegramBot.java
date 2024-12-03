package com.pincio.telegramwebhook.service;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MyTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    public MyTelegramBot(String botToken, String botUsername) {
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Non gestiamo aggiornamenti in tempo reale, il webhook fa già il lavoro
    }
}
