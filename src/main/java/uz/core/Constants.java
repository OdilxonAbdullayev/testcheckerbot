package uz.core;


public interface Constants {

    interface BotCommand {
        String CALL_MAIN_MENU = "mainMenu";
        String CALL_LANG = "language=";
        String ADD_CHANNEL = "addChannel";
        String DELETE_CHANNEL = "channel-d=";
        String ADD_ADMIN = "addAdmin";
        String DELETE_ADMIN = "admin-d=";
        String CALLBACK_EMPTY = "CALLBACK_EMPTY";
        String BACK_BUTTON_TEXT = "⬅\uFE0F Orqaga qaytish";
        String BUTTON_DELETE = "\uD83D\uDDD1 O'chirish";
        String BUTTON_ADD = "➕ Qo'shish";

        String BUTTON_STATISTIC = "\uD83D\uDCCA Statistika";
        String BUTTON_SEND_MESSAGE = "\uD83D\uDCE4 Xabar yuborish";
        String CALL_SEND_MESSAGE = "SEND-MESSAGE";
        String CALL_CHANNEL_RESULT = "channelResult";
        String TEXT_DOING = "✅ Xabar yuborish kutilmoqda...";
        String BUTTON_CHANNEL = "\uD83D\uDCE1 Kanallar";
        String BUTTON_ADMIN = "\uD83D\uDC68\u200D\uD83D\uDD27 Adminlar";
        String CREATE_TEST = "➕Test yaratish";
        String MILLIY_SERTIFIKAT = "MilliySertifikat";
        String ATTESTATSIYA = "Attestatsiya";

    }

    interface ParseMode {
        String HTML = "HTML";
        String MARKDOWN = "Markdown";
        String MARKDOWN_V2 = "MarkdownV2";
    }

    interface ResponseMessage {
        String ERROR = "ERROR: ";
        String SYSTEM_ERROR = "<b>\uD83D\uDEA8 Tizimda xatolik sodir bo'ldi!</b>";
        String PLEASE_ENTER_ONLY_NUMBER = "Iltimos faqat raqam kiriting!";
        String ERROR_SQL_DELETE = "ERROR: DELETE Sql id is not found";
        String ERROR_SQL_SELECT = "ERROR: SELECT Sql id is not found";
        String ERROR_SQL_INSERT = "ERROR: INSERT Sql id is not found";
        String ERROR_SQL_UPDATE = "ERROR: UPDATE Sql id is not found";
    }

}
