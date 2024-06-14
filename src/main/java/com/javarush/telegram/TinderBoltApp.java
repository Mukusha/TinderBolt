package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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

    // в config.properties прописать параметры
    public static final String TELEGRAM_BOT_NAME = property.getProperty("bot.name");
    public static final String TELEGRAM_BOT_TOKEN = property.getProperty("bot.token");
    public static final String OPEN_AI_TOKEN = property.getProperty("OPEN_AI_TOKEN");

    private DialogMode currentMode = null;
    private final ArrayList<String> list = new ArrayList<>();
    private final ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("Главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }

        // GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT) {
            String promt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает ... ");
            String answer = chatGPT.sendMessage(promt, message);
            updateTextMessage(msg, answer);
            return;
        }

        //DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде \uD83D\uDD25", "date_grande",
                    "Марго Робби 🔥🔥", "date_robby",
                    "Зендея     \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25", "date_zendaya",
                    "Райан Гослинг \uD83D\uDE0E", "date_gosling",
                    "Том Харди   \uD83D\uDE0E\uD83D\uDE0E", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор!\nТвоя задача пригласить девушку на свидание❤️ за 5 сообщений!");

                String promt = loadPrompt(query);
                chatGPT.setPrompt(promt);
                return;
            }

            Message msg = sendTextMessage("Подождите, девушка набирает текст ... ");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }


        // MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            String text = loadMessage("message");
            sendTextButtonsMessage(text, "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                sendPhotoMessage(query);
                String promt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает ... ");
                String answer = chatGPT.sendMessage(promt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);


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
