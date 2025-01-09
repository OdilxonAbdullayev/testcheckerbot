package uz.telegram;

import uz.core.utils.PropertiesUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.filter.AuthenticationFilterController;

public class TelegramBot extends TelegramLongPollingBot {
    {
        BaseTelegramBot.getInstance(this);
    }


    @Override
    public void onUpdateReceived(Update update) {
        new Thread(() -> {
            new AuthenticationFilterController(update);
        }).start();
    }

    @Override
    public String getBotUsername() {
        return PropertiesUtils.getTelegramBotUsername();
    }

    @Override
    public String getBotToken() {
        return PropertiesUtils.getTelegramBotToken();
    }

    @Override
    public String getBaseUrl() {
        return PropertiesUtils.getTelegramBaseUrl() + getBotToken() + "/";
    }
}
