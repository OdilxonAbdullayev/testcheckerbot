package uz.telegram.service;

import uz.core.Constants;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.AdminEntity;
import uz.db.entity.AnswerEntity;
import uz.db.entity.ChannelEntity;
import uz.db.entity.UserEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.db.enums.QuizType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class KeyboardService {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

    public static InlineKeyboardMarkup getChannelButton(List<String> list, TextService textService) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        list.forEach(entity -> {
            InlineKeyboardButton schoolButton = new InlineKeyboardButton();
            schoolButton.setText(textService.getJoinChannelText());
            schoolButton.setUrl(entity);
            buttons.add(List.of(schoolButton));
        });

        InlineKeyboardButton resultButton = new InlineKeyboardButton();

        resultButton.setText(textService.getCheckChannelText());
        resultButton.setCallbackData(Constants.BotCommand.CALL_CHANNEL_RESULT);

        buttons.add(List.of(resultButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup backButton(String key) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(Constants.BotCommand.BACK_BUTTON_TEXT);
        inlineKeyboardButton.setCallbackData(key);

        inlineKeyboardMarkup.setKeyboard(List.of(
                List.of(inlineKeyboardButton)
        ));

        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getMainKeyboard(UserEntity user) {
        ReplyKeyboardMarkup reply = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();


        KeyboardButton statisticButton = new KeyboardButton(Constants.BotCommand.BUTTON_STATISTIC);
        KeyboardButton channelButton = new KeyboardButton(Constants.BotCommand.BUTTON_CHANNEL);
        KeyboardButton adminButton = new KeyboardButton(Constants.BotCommand.BUTTON_ADMIN);
        KeyboardButton sendMessageButton = new KeyboardButton(Constants.BotCommand.BUTTON_SEND_MESSAGE);
        KeyboardButton createTest = new KeyboardButton(Constants.BotCommand.CREATE_TEST);
        KeyboardRow keyboardButtons = new KeyboardRow();

        rows.add(keyboardButtons);

        if (PropertiesUtils.getAdmins().stream().anyMatch(adminEntity -> adminEntity.getId().equals(user.getId()))) {
            keyboardButtons.add(createTest);
            rows.add(new KeyboardRow(List.of(adminButton, statisticButton)));
            rows.add(new KeyboardRow(List.of(channelButton, sendMessageButton)));
        }

        reply.setKeyboard(rows);
        reply.setResizeKeyboard(true);
        return reply;
    }

    public static InlineKeyboardMarkup getChannels(List<ChannelEntity> channels) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        channels.forEach(channelEntity -> {
            InlineKeyboardButton schoolButton = new InlineKeyboardButton();

            schoolButton.setText(String.valueOf(channelEntity.getId()));
            schoolButton.setCallbackData(Constants.BotCommand.CALLBACK_EMPTY);

            InlineKeyboardButton deleteButton = new InlineKeyboardButton();

            deleteButton.setText(Constants.BotCommand.BUTTON_DELETE);
            deleteButton.setCallbackData(Constants.BotCommand.DELETE_CHANNEL + channelEntity.getId());

            buttons.add(List.of(schoolButton, deleteButton));
        });

        InlineKeyboardButton backButton = new InlineKeyboardButton();

        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);

        InlineKeyboardButton addButton = new InlineKeyboardButton();

        addButton.setText(Constants.BotCommand.BUTTON_ADD);
        addButton.setCallbackData(Constants.BotCommand.ADD_CHANNEL);

        buttons.add(List.of(addButton, backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }


    public static InlineKeyboardMarkup getAdmins(List<AdminEntity> admins) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        admins.forEach(channelEntity -> {
            InlineKeyboardButton schoolButton = new InlineKeyboardButton();

            schoolButton.setText(String.valueOf(channelEntity.getId()));
            schoolButton.setCallbackData(Constants.BotCommand.CALLBACK_EMPTY);

            InlineKeyboardButton deleteButton = new InlineKeyboardButton();

            deleteButton.setText(Constants.BotCommand.BUTTON_DELETE);
            deleteButton.setCallbackData(Constants.BotCommand.DELETE_ADMIN + channelEntity.getId());

            buttons.add(List.of(schoolButton, deleteButton));
        });

        InlineKeyboardButton backButton = new InlineKeyboardButton();

        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);

        InlineKeyboardButton addButton = new InlineKeyboardButton();

        addButton.setText(Constants.BotCommand.BUTTON_ADD);
        addButton.setCallbackData(Constants.BotCommand.ADD_ADMIN);

        buttons.add(List.of(addButton, backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getQuizType() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton attestatsiya = new InlineKeyboardButton();
        attestatsiya.setText(String.valueOf(QuizType.ATTESTATSIYA));
        attestatsiya.setCallbackData(Constants.BotCommand.ATTESTATSIYA);

        InlineKeyboardButton milliy = new InlineKeyboardButton();
        milliy.setText(String.valueOf(QuizType.MILLIY_SERTIFIKAT));
        milliy.setCallbackData(Constants.BotCommand.MILLIY_SERTIFIKAT);

        buttons.add(List.of(attestatsiya, milliy));

        InlineKeyboardButton backButton = new InlineKeyboardButton();

        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);

        buttons.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

}
