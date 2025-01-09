package uz.telegram.handler;

import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import uz.core.utils.AppUtils;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.*;
import uz.db.respository.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.MessageService;
import uz.telegram.service.TextService;
import uz.core.Constants;

import java.util.*;


public class CallbackHandler {
    private final LogManager _logger = new LogManager(CallbackHandler.class);
    private final UserEntity user;
    private final CallbackQuery callbackQuery;
    private final MessageService messageService;

    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final ChannelRepository channelRepository = ChannelRepository.getInstance();
    private static final AdminRepository adminRepository = AdminRepository.getInstance();
    private ResourceBundle rb;
    private TextService textService;

    public CallbackHandler(UserEntity user, CallbackQuery callbackQuery, ResourceBundle rb) {
        this.user = user;
        this.rb = rb;
        this.textService = new TextService(rb);
        this.callbackQuery = callbackQuery;
        this.messageService = new MessageService(user);
    }

    public void handler() {
        switch (getData()) {
            case Constants.BotCommand.CALL_CHANNEL_RESULT, Constants.BotCommand.CALL_MAIN_MENU -> {
                deleteMessage();
                user.setStep(null);
                userRepository.update(user);
                messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));
                return;
            }
        }

        if (getData().contains(Constants.BotCommand.CALL_LANG)) {
            deleteMessage();
            userRepository.update(user);
            textService = new TextService(ResourceBundle.getBundle("Translate", new Locale("uz")));
            messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));
            return;
        }

        if (PropertiesUtils.getAdmins().stream().anyMatch(adminEntity -> adminEntity.getId().equals(user.getId()))) {
            switch (getData()) {
                case Constants.BotCommand.ADD_CHANNEL -> {
                    deleteMessage();
                    user.setStep(Constants.BotCommand.ADD_CHANNEL);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.enterData("Kanal idisini"), KeyboardService.backButton(Constants.BotCommand.CALL_MAIN_MENU));
                    return;
                }
                case Constants.BotCommand.ADD_ADMIN -> {
                    deleteMessage();
                    user.setStep(Constants.BotCommand.ADD_ADMIN);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.enterData("Admin idisini"), KeyboardService.backButton(Constants.BotCommand.CALL_MAIN_MENU));
                    return;
                }
                case Constants.BotCommand.ATTESTATSIYA -> {
                    deleteMessage();
                    user.setStep(Constants.BotCommand.ATTESTATSIYA);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.getCreateTest(""), KeyboardService.backButton(Constants.BotCommand.CALL_MAIN_MENU));
                }
                case Constants.BotCommand.MILLIY_SERTIFIKAT -> {
                    deleteMessage();
                    user.setStep(Constants.BotCommand.MILLIY_SERTIFIKAT);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.getCreateTest("\n\n <b>Eslatma❗️Milliy sertifikat uchun javoblar 35 ta bo'lishi kerak</b>"), KeyboardService.backButton(Constants.BotCommand.CALL_MAIN_MENU));
                }
            }

            if (getData().contains(Constants.BotCommand.DELETE_CHANNEL)) {
                Long channelId = Long.valueOf(getData().split("=")[1]);
                deleteMessage();
                channelRepository.delete(channelId);
                DDLResponse<List<ChannelEntity>> response = channelRepository.getList(Map.of());
                if (!response.getStatus()) {
                    messageService.sendError(getChatId(), response);
                    return;
                }
                user.setStep(null);
                userRepository.update(user);
                messageService.sendMessage(getChatId(), textService.getChooseButton("Kannallardan"), KeyboardService.getChannels(response.getData()));
                return;
            } else if (getData().contains(Constants.BotCommand.DELETE_ADMIN)) {
                Long channelId = Long.valueOf(getData().split("=")[1]);
                deleteMessage();
                adminRepository.delete(channelId);
                DDLResponse<List<AdminEntity>> response = adminRepository.getList(Map.of());
                if (!response.getStatus()) {
                    messageService.sendError(getChatId(), response);
                    return;
                }
                user.setStep(null);
                userRepository.update(user);
                messageService.sendMessage(getChatId(), textService.getChooseButton("Adminlardan"), KeyboardService.getAdmins(response.getData()));
                return;
            }
        }

    }

    public void deleteMessage() {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(getChatId());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());

        try {
            BaseTelegramBot.getSender().execute(deleteMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public String getData() {
        return callbackQuery.getData();
    }

    public Long getChatId() {
        return callbackQuery.getMessage().getChatId();
    }

    public String getCallBackQueryId() {
        return callbackQuery.getId();
    }

}
