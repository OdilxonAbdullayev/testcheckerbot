package uz.telegram.service;

import uz.core.Constants;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.*;
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
        KeyboardButton information = new KeyboardButton(Constants.BotCommand.INFO);
        KeyboardButton mySubjects = new KeyboardButton(Constants.BotCommand.MY_TESTS);
        KeyboardButton createTest = new KeyboardButton(Constants.BotCommand.CREATE_TEST);

        if (PropertiesUtils.getAdmins().stream().anyMatch(adminEntity -> adminEntity.getId().equals(user.getId()))) {
            rows.add(new KeyboardRow(List.of(createTest, mySubjects)));
            rows.add(new KeyboardRow(List.of(adminButton, statisticButton)));
            rows.add(new KeyboardRow(List.of(channelButton, sendMessageButton)));
        }
        rows.add(new KeyboardRow(List.of(information)));

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

    public static InlineKeyboardMarkup getTests(List<SubjectEntity> subjectEntitiesList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int i = 0; i < subjectEntitiesList.size(); i++) {
            SubjectEntity subject = subjectEntitiesList.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(String.valueOf(subject.getName()));
            button.setCallbackData(Constants.BotCommand.TESTS + subject.getSecurity_key());

            currentRow.add(button);

            if (currentRow.size() == 2 || i == subjectEntitiesList.size() - 1) {
                buttons.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);
        buttons.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }


    public static InlineKeyboardMarkup quizDeleteButton(String security_key) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText(Constants.BotCommand.BUTTON_DELETE);
        deleteButton.setCallbackData(Constants.BotCommand.DELETE_TEST + security_key);

        InlineKeyboardButton userAnswersList = new InlineKeyboardButton();
        userAnswersList.setText("\uD83D\uDCE5Yuklab olish");
        userAnswersList.setCallbackData(Constants.BotCommand.DOWNLOAD + security_key);

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);
        buttons.add(List.of(userAnswersList, deleteButton));
        buttons.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getFilter() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton allTest = new InlineKeyboardButton();
        allTest.setText(Constants.BotCommand.ALL_TEST);
        allTest.setCallbackData(Constants.BotCommand.SHOW_ALL_TEST);

        InlineKeyboardButton filteredTest = new InlineKeyboardButton();
        filteredTest.setText(Constants.BotCommand.FILTER_TEST);
        filteredTest.setCallbackData(Constants.BotCommand.SHOW_FILTER_TEST);

        buttons.add(List.of(allTest, filteredTest));

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);
        buttons.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getFilterType() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        InlineKeyboardButton bySubjectName = new InlineKeyboardButton();
        bySubjectName.setText(Constants.BotCommand.BY_SUBJECT_NAME);
        bySubjectName.setCallbackData(Constants.BotCommand.SHOW_BY_SUBJECT_NAME);
        buttons.add(List.of(bySubjectName));

        InlineKeyboardButton attestatsiya = new InlineKeyboardButton();
        attestatsiya.setText(Constants.BotCommand.BY_ATTESTATSIYA);
        attestatsiya.setCallbackData(Constants.BotCommand.BY_ATTESTATSIYA);

        InlineKeyboardButton milliy_sertifikat = new InlineKeyboardButton();
        milliy_sertifikat.setText(Constants.BotCommand.BY_MILLIY);
        milliy_sertifikat.setCallbackData(Constants.BotCommand.BY_MILLIY);

        buttons.add(List.of(attestatsiya, milliy_sertifikat));

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(Constants.BotCommand.BACK_BUTTON_TEXT);
        backButton.setCallbackData(Constants.BotCommand.CALL_MAIN_MENU);
        buttons.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(buttons);

        return inlineKeyboardMarkup;
    }

}
