package com.Aga.Agali.bot;

import com.Aga.Agali.service.BotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class CvBot extends TelegramLongPollingBot {

    private final BotService botService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public CvBot(@Value("${telegram.bot.token}") String botToken, BotService botService) {
        super(botToken);
        this.botService = botService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            botService.handleUpdate(update);
        } catch (Exception e) {
            log.error("Update handle xətası: {}", e.getMessage(), e);
        }
    }
    public String getBotToken() {
        return super.getBotToken();
    }
}