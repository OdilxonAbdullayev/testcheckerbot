package uz.telegram.handler;

import uz.core.logger.LogManager;
import uz.db.entity.UserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.telegram.service.MessageService;

import java.util.ResourceBundle;

public class UpdateHandler {
    private final LogManager _logger = new LogManager(UpdateHandler.class);
    private final UserEntity user;
    private final MessageService messageService;
    private final Update update;
    private ResourceBundle rb;

    public UpdateHandler(UserEntity user, Update update, ResourceBundle rb) {
        this.user = user;
        this.rb = rb;
        this.update = update;
        this.messageService = new MessageService(user);
    }

    public void handler() {
        try {
            if (update.hasMessage()) {
                new MessageHandler(user, update.getMessage(), rb).handler();
            } else if (update.hasCallbackQuery()) {
                new CallbackHandler(user, update.getCallbackQuery(), rb).handler();
            } else {
                _logger.info("Unsupported update: ");
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public Long getChatId() {
        return update.getMessage().getChatId();
    }
}
