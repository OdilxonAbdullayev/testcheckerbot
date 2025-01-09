package uz.telegram.service;

import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.core.Constants;
import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.AdminEntity;
import uz.db.entity.UserEntity;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.telegram.core.BaseTelegramBot;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class MessageService {
    private final LogManager _logger = new LogManager(MessageService.class);
    private final UserEntity user;

    public MessageService(UserEntity user) {
        this.user = user;
    }

    public void sendError(Long chatId, DDLResponse response) {
        _logger.error(response.getLogMessage());
        sendMessage(chatId, response.getResponseMessage());
    }

    public void sendMessage(Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setParseMode("HTML");

            BaseTelegramBot.getSender().execute(sendMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void sendMessageToAdmin(UserEntity user, List<AdminEntity> admins, String text) {
        String user_info = "Id: %s \nIsm familiya: %s \n\n".formatted(user.getId(), user.getUsername());

        try {
            for (AdminEntity admin : admins) {
                SendMessage sendMessage = new SendMessage();

                sendMessage.setChatId(admin.getId());
                sendMessage.setText(user_info + text);
                sendMessage.setParseMode("HTML");

                BaseTelegramBot.getSender().execute(sendMessage);
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public Message sendMessage(Long chatId, String text, Boolean isProtected) {
        try {
            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setParseMode("HTML");

            return BaseTelegramBot.getSender().execute(sendMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setParseMode(Constants.ParseMode.HTML);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

            BaseTelegramBot.getSender().execute(sendMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }


    private String checker(String value, Integer maxLength) {
        if (value.length() > maxLength) {
            return "Siz kiritgan qiymat %d - ta belgidan oshmasligi kerak!".formatted(maxLength);
        }
        return null;
    }

    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup replyKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setParseMode(Constants.ParseMode.HTML);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

            BaseTelegramBot.getSender().execute(sendMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void showAlert(String text, String callbackQueryId) {
        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

            answerCallbackQuery.setCallbackQueryId(callbackQueryId);
            answerCallbackQuery.setText(text);
            answerCallbackQuery.setShowAlert(true);

            BaseTelegramBot.getSender().execute(answerCallbackQuery);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void notShowAlert(String text, String callbackQueryId) {
        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

            answerCallbackQuery.setCallbackQueryId(callbackQueryId);
            answerCallbackQuery.setText(text);
            answerCallbackQuery.setShowAlert(false);

            BaseTelegramBot.getSender().execute(answerCallbackQuery);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void copyMessage(Long chatId, Long fromChatId, Integer messageId) {
        try {
            CopyMessage copyMessage = new CopyMessage();

            String mainChatId = String.valueOf(fromChatId);

            if (!mainChatId.startsWith("-100")) {
                mainChatId = "-100" + mainChatId;
            }

            copyMessage.setFromChatId(mainChatId);
            copyMessage.setChatId(chatId);
            copyMessage.setMessageId(messageId);

            BaseTelegramBot.getSender().execute(copyMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void copyMessage(Long chatId, Long fromChatId, Long messageId, String text) {
        try {
            CopyMessage copyMessage = new CopyMessage();

            String mainChatId = String.valueOf(fromChatId);

            if (!mainChatId.startsWith("-100")) {
                mainChatId = "-100" + mainChatId;
            }

            copyMessage.setFromChatId(mainChatId);
            copyMessage.setChatId(chatId);
            copyMessage.setMessageId(Math.toIntExact(messageId));
            copyMessage.setCaption(text);
            copyMessage.setParseMode(Constants.ParseMode.HTML);

            BaseTelegramBot.getSender().execute(copyMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public Message sendVideo(Long chatid, String url, String fileName) {
        try {
            URL targetUrl = new URL(url);
            try (InputStream stream = targetUrl.openStream()) {
                SendVideo sendVideo = new SendVideo();

                InputFile inputFile = new InputFile(stream, fileName);

                sendVideo.setChatId(chatid);
                sendVideo.setVideo(inputFile);

                return BaseTelegramBot.getSender().execute(sendVideo);
            } catch (
                    Exception e) {
                _logger.error(e.getMessage());
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public Message sendAudio(Long chatid, String url, String fileName) {
        try {
            URL targetUrl = new URL(url);
            try (InputStream stream = targetUrl.openStream()) {
                SendAudio sendVideo = new SendAudio();

                InputFile inputFile = new InputFile(stream, fileName);

                sendVideo.setChatId(chatid);
                sendVideo.setAudio(inputFile);

                return BaseTelegramBot.getSender().execute(sendVideo);
            } catch (
                    Exception e) {
                _logger.error(e.getMessage());
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public Message sendAudio(Long chatid, String path, boolean isFile) {
        try {
            SendAudio sendVideo = new SendAudio();

            InputFile inputFile = new InputFile(new File(path));

            sendVideo.setChatId(chatid);
            sendVideo.setAudio(inputFile);

            return BaseTelegramBot.getSender().execute(sendVideo);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public Message sendVideo(Long chatid, String path, boolean isFile) {
        try {
            SendVideo sendVideo = new SendVideo();

            InputFile inputFile = new InputFile(new File(path));

            sendVideo.setChatId(chatid);
            sendVideo.setVideo(inputFile);

            return BaseTelegramBot.getSender().execute(sendVideo);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public Message sendVideo(Long chatid, String url) {
        try {
            SendVideo sendVideo = new SendVideo();
            InputFile inputFile = new InputFile(url);
            sendVideo.setChatId(chatid);
            sendVideo.setVideo(inputFile);
            return BaseTelegramBot.getSender().execute(sendVideo);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public Integer sendVideo(long chatId, String url) {
        String telegramApiUrl = "https://api.telegram.org/bot" + PropertiesUtils.getTelegramBotToken() + "/sendVideo?chat_id=" + chatId + "&video=" + url;

        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(telegramApiUrl))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            return new JSONObject(response.body()).getJSONObject("result").getInt("message_id");
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
        return null;
    }

    public void deleteMessage(Long chatid, Integer messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatid);
            deleteMessage.setMessageId(messageId);
            BaseTelegramBot.getSender().execute(deleteMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }
}
