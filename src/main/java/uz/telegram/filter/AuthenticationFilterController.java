package uz.telegram.filter;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.core.logger.LogManager;
import uz.core.utils.AppUtils;
import uz.db.entity.UserEntity;
import uz.db.respository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.handler.UpdateHandler;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.MessageService;
import uz.telegram.service.TextService;

import java.util.*;

public class AuthenticationFilterController {
    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final List<String> keys = new ArrayList<>();
    private static final LogManager _logger = new LogManager(AuthenticationFilterController.class);

    public AuthenticationFilterController(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            Long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();

            Optional<UserEntity> optionalUser = userRepository.getOne(new HashMap<>() {{
                put("id", chatId);
            }}).getData();


            if (optionalUser.isPresent()) {
                UserEntity user = optionalUser.get();
                if (update.hasMessage() && update.getMessage().getText().startsWith("/start ")) {
                    String security_key = update.getMessage().getText().split(" ")[1];
                    user.setCurrent_security_key(security_key);
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "Yaxshi, endi test javoblarini yuboring❗️");
                    try {
                        BaseTelegramBot.getSender().execute(sendMessage);
                    } catch (Exception e) {
                        _logger.error(e.getMessage());
                    }
                    userRepository.update(user);
                }
                user.setStatus_id(1);
                ResourceBundle rb = ResourceBundle.getBundle("Translate", new Locale("uz"));

                if (!AppUtils.isJoinedChannel(user.getId(), new TextService(rb))) {
                    if (update.hasCallbackQuery()) {
                        try {
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            BaseTelegramBot.getSender().execute(new DeleteMessage(String.valueOf(chatId), messageId));
                        } catch (TelegramApiException e) {
                            _logger.error(e.getMessage());
                        }
                    }
                    return;
                }

                new UpdateHandler(user, update, rb).handler();
            } else {
                if (update.getMessage().getText().startsWith("/start ")) {
                    String security_key = update.getMessage().getText().split(" ")[1];
                    keys.add(security_key);
                }
                if (update.hasMessage() && update.getMessage().getText().startsWith("fio*")) {
                    UserEntity entity = new UserEntity();
                    entity.setId(chatId);
                    entity.setStatus_id(1);
                    if (keys.size() != 0) {
                        entity.setCurrent_security_key(keys.get(0));
                    }
                    entity.setUsername(update.getMessage().getText().substring(4).trim());
                    userRepository.insert(entity);
                    ReplyKeyboardMarkup mainKeyboard = KeyboardService.getMainKeyboard(entity);
                    SendMessage sendMessage = new SendMessage(chatId.toString(), "<b>\uD83D\uDC4BAssalomu alaykum \n\nBotimizga xush kelibsiz botimz faqat maxsus link orqali kirganda ishlaydi iltimos maxsus link orqali kiring va testlaringizni javobini yuboring</b>");
                    sendMessage.setParseMode(ParseMode.HTML);
                    sendMessage.setReplyMarkup(mainKeyboard);

                    mainKeyboard.setResizeKeyboard(true);
                    try {
                        BaseTelegramBot.getSender().execute(sendMessage);
                    } catch (Exception e) {
                        _logger.error(e.getMessage());
                    }
                    if (keys.size() != 0) {
                        SendMessage sendMessage2 = new SendMessage(chatId.toString(), "Endi test javoblarini yuboring❗️");
                        try {
                            BaseTelegramBot.getSender().execute(sendMessage2);
                        } catch (Exception e) {
                            _logger.error(e.getMessage());
                        }
                    }
                    return;
                }
                ResourceBundle rb = ResourceBundle.getBundle("Translate", new Locale("uz"));
                try {
                    TextService textService = new TextService(rb);
                    String username = textService.getUsernameExample();
                    SendMessage sendMessage = new SendMessage(chatId.toString(), username);
                    sendMessage.setParseMode(ParseMode.MARKDOWN);
                    BaseTelegramBot.getSender().execute(sendMessage);
                } catch (Exception e) {
                    _logger.error(e.getMessage());
                }
//                new UpdateHandler(entity, update, ResourceBundle.getBundle("Translate", new Locale("uz"))).handler();
            }


        }
    }

}
