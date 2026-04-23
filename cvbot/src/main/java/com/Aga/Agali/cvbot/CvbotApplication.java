package com.Aga.Agali.cvbot;

import com.Aga.Agali.bot.CvBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EntityScan(basePackages = "com.Aga.Agali")
@ComponentScan(basePackages = "com.Aga.Agali")
@EnableJpaRepositories(basePackages = "com.Aga.Agali")
public class CvbotApplication {

    public static void main(String[] args) throws TelegramApiException {
        ApplicationContext context = SpringApplication.run(CvbotApplication.class, args);

        System.out.println(">>> Bot qeydiyyata başlayır...");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean(CvBot.class));
        System.out.println(">>> Bot qeydiyyat tamamlandı!");
    }

}