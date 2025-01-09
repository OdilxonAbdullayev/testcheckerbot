package uz.telegram.core;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class BaseTelegramBot {
    private static final BaseTelegramBot instance = new BaseTelegramBot();
    @Getter
    @Setter
    private static AbsSender sender;


    public static BaseTelegramBot getInstance(AbsSender absSender) {
        setSender(absSender);
        return instance;
    }


}
