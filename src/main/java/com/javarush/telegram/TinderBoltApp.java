package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TinderBoltApp extends MultiSessionTelegramBot {
    static Properties property = new Properties();

    static {

        FileInputStream fis;
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            property.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String TELEGRAM_BOT_NAME = property.getProperty("bot.name");
    public static final String TELEGRAM_BOT_TOKEN = property.getProperty("bot.token");
    public static final String OPEN_AI_TOKEN = property.getProperty("OPEN_AI_TOKEN");


    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("Вы написали " + message);

        sendTextButtonsMessage("Выберите режим работы",
                "Старт", "start",
                "Стоп", "stop");

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
