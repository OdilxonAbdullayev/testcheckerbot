package uz.telegram.service;


import uz.db.entity.AllStatisticEntity;
import uz.db.entity.AnswerEntity;
import uz.db.entity.SubjectEntity;
import uz.db.enums.QuizType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class TextService {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
    public static final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    private ResourceBundle rb;

    public TextService(ResourceBundle rb) {
        this.rb = rb;
    }

    public String getCheckChannelText() {
        return rb.getString("text.channel.check");
    }

    public String getChooseChannelText() {
        return rb.getString("text.channel.text");
    }

    public String getJoinChannelText() {
        return rb.getString("text.channel.join");
    }

    public String enterData(String data) {
        return "✍\uFE0F %s kiriting: ".formatted(data);
    }

    public String startMessage() {
        return rb.getString("text.start");
    }

    public String getChooseButton(String targetName) {
        return "<b>\uD83D\uDC47 Quyidagi <b>%s</b> birini tanlang: </b>".formatted(targetName);
    }


    public String getUsernameExample() {
        return "Hurmatli foydalanuvchi ism familiyangizni quyidagicha  kiriting:\n\n``` fio*familiya ͏ism ```\nMisol:\n``` fio*Abdullayev ͏Odilxon ```";
    }

    public String subjectNotFound() {
        return "Xatolik!\n" +
               "Test kodini noto`g`ri yuborgan bo`lishingiz mumkin, iltimos tekshirib qaytadan yuboring.";
    }

    public String errorSendAnswerCount(String security_key, int getAnswerLength, int allAnswerLength) {
        return "%s kodli testda savollar soni %d ta.\n❌Siz esa %d ta javob yozdingiz!".formatted(security_key, allAnswerLength, getAnswerLength);

    }

    public String getResult(String securityKey, String name, int size, int correctCount, int incorrectCount, double accuracyPercentage, double ball, QuizType quizType) {
        return "Natija\uD83D\uDC47 \n\n\uD83D\uDCDA Fan: %s\n\uD83D\uDD11 Test kodi: %s\n♾️Test turi: %s \n✏️ Jami savollar soni: %d ta \n✅ To'g'ri javoblar soni: %d ta \n❓Noto`g`ri javoblaringiz: %d \n\uD83D\uDD1DBall: %s \n\uD83D\uDD23Foiz : %s"
                .formatted(name, securityKey, quizType.getDisplayName(), size, correctCount, incorrectCount, String.format("%.1f", ball), String.format("%.2f", accuracyPercentage) + "%");
    }

    public String getStatistic(AllStatisticEntity entity) {
        return ("<b>\uD83D\uDCCA Bot statistikasi</b>" +
                "\n\nJami foydalanuvchilar: <b>%d - ta</b>" +
                "\nFaol foydalanuvchilar: <b>%d - ta</b>" +
                "\nO'chirilgan foydalanuvchilar: <b>%d - ta</b>")
                .formatted(
                        entity.getAll_user_count(),
                        entity.getActive_user_count(),
                        entity.getDeleted_user_count()
                );
    }

    public String getCreateTest(String targetText) {
        return "\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47 Test yaratish uchun yo'riqnoma. \n\ntest*Fan nomi*to'g'ri javoblar \n\nMisol: \ntest*Informatika*abbccdd \ntest*Ona tili*abcdabcd %s".formatted(targetText);
    }

    public String errorCreateTest(String targetText) {
        return "❌Xatolik \n\nTog'ri formatda yubormadingiz❗️ %s".formatted(targetText);
    }

    public String createTestSuccess(SubjectEntity savedSubject, int length, String creator_user) {
        return "<b>✅Test bazaga qo`shildi.\n\nTest kodi: %s \n\uD83D\uDD17Test link: https://t.me/translateentouzbot?start=%s \nSavollar soni: %s ta.\nTest yaratuvchisi: %s \nFan: %s \n </b>"
                .formatted(savedSubject.getSecurity_key(), savedSubject.getSecurity_key(), length, creator_user, savedSubject.getName());
    }

    public String getInfoButton() {
        return "<b>\uD83D\uDC4B Assalomu Alaykum aziz foydalanuvchi! \n\nBotimizga xush kelibsiz! Agarda botimizdan foydalanishga qiynalsangiz quyidagilar bilan tanishib chiqing! \n\n✅ Botimizga maxsus link bilan /start bosing, so'ngra test javoblarini yuboring. \n\nAgarda kanalga obuna bo'lish so‘ralsa, pasdagi kanallarga obuna bo'lib «✅ Kanalga Obuna Bo'ldim» tugmasiga bosing.\n\n \uD83E\uDDD1\u200D\uD83D\uDCBB Bot Dasturchisi: <a href=\"https://t.me/XDasturchi\">𝑿 𝑫 𝒂 𝒔 𝒕 𝒖 𝒓 𝒄 𝒉 𝒊</a> </b>";
    }

}
