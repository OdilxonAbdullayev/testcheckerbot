package uz;

import uz.core.logger.LogManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.telegram.TelegramBot;

public class Main {
    private static final LogManager _logger = new LogManager(Main.class);

    public static void main(String[] args) {
        try {
            _logger.info("Starting...");
            TelegramBotsApi telegram = new TelegramBotsApi(DefaultBotSession.class);
            telegram.registerBot(new TelegramBot());
            _logger.info("Started!!!");
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }
}
