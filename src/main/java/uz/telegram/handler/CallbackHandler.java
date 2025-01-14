package uz.telegram.handler;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import uz.core.utils.AppUtils;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.*;
import uz.db.enums.QuizType;
import uz.db.respository.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.MessageService;
import uz.telegram.service.TextService;
import uz.core.Constants;

import javax.security.auth.Subject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


public class CallbackHandler {
    private final LogManager _logger = new LogManager(CallbackHandler.class);
    private final UserEntity user;
    private final CallbackQuery callbackQuery;
    private final MessageService messageService;

    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final ChannelRepository channelRepository = ChannelRepository.getInstance();
    private static final AdminRepository adminRepository = AdminRepository.getInstance();
    private static final SubjectRepository subjectRepository = SubjectRepository.getInstance();
    private static final AnswerRepository answerRepository = AnswerRepository.getInstance();
    private static final UserAnswerRepository userAnswerRepository = UserAnswerRepository.getInstance();
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
                user.setCurrent_security_key(null);
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
            } else if (getData().contains(Constants.BotCommand.TESTS)) {
                String security_key = String.valueOf(getData().split("=")[1]);
                deleteMessage();
                Optional<SubjectEntity> optionalSubject = subjectRepository.getOne(new HashMap<>() {{
                    put("security_key", security_key);
                }}).getData();

                if (optionalSubject.isPresent()) {
                    SubjectEntity subjectEntity = optionalSubject.get();
                    List<AnswerEntity> allAnswerBySubjectId = AppUtils.getAllAnswerBySubjectId(subjectEntity.getId());

                    UserEntity userEntity = userRepository.getOne(new HashMap<>() {{
                        put("id", subjectEntity.getCreated_user_id());
                    }}).getData().orElseThrow();

                    messageService.sendMessage(getChatId(), textService.getTestInfo(subjectEntity, allAnswerBySubjectId.size(), userEntity.getUsername(), subjectEntity.getQuiz_type(), allAnswerBySubjectId), KeyboardService.quizDeleteButton(subjectEntity.getSecurity_key()));
                    user.setStep(null);
                    userRepository.update(user);
                    return;
                } else {
                    _logger.error("Not found this security_key!");
                }
                return;
            } else if (getData().contains(Constants.BotCommand.DELETE_TEST)) {
                String security_key = String.valueOf(getData().split("=")[1]);
                deleteMessage();
                Optional<SubjectEntity> optionalSubject = subjectRepository.getOne(new HashMap<>() {{
                    put("security_key", security_key);
                }}).getData();

                if (optionalSubject.isPresent()) {
                    SubjectEntity subjectEntity = optionalSubject.get();
                    subjectEntity.setIs_delete(1);

                    subjectRepository.update(subjectEntity);

                    user.setStep(null);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));
                    return;
                } else {
                    _logger.error("Not found this subject by security_key!");
                }
            } else if (getData().contains(Constants.BotCommand.SHOW_ALL_TEST)) {
                deleteMessage();
                DDLResponse<List<SubjectEntity>> response = subjectRepository.getList(Map.of());
                if (!response.getStatus()) {
                    messageService.sendError(getChatId(), response);
                    return;
                }
                for (int i = 0; i < response.getData().size(); i++) {
                    messageService.sendMessage(getChatId(), textService.getChooseButton("testlardan"), KeyboardService.getTests(response.getData()));
                    return;
                }
                return;
            } else if (getData().contains(Constants.BotCommand.SHOW_FILTER_TEST)) {
                deleteMessage();
                messageService.sendMessage(getChatId(), textService.getFilterType(), KeyboardService.getFilterType());
                return;
            } else if (getData().contains(Constants.BotCommand.SHOW_BY_SUBJECT_NAME)) {
                deleteMessage();
                user.setStep(Constants.BotCommand.SHOW_BY_SUBJECT_NAME);
                userRepository.update(user);
                messageService.sendMessage(getChatId(), "Yaxshi, endi fan nomini yuboring!", KeyboardService.backButton(Constants.BotCommand.CALL_MAIN_MENU));
                return;
            } else if (getData().contains(Constants.BotCommand.BY_ATTESTATSIYA)) {
                deleteMessage();
                List<SubjectEntity> subjectEntitiesEqualQuizType = AppUtils.getAttestatsiyaList();
                if (subjectEntitiesEqualQuizType != null && subjectEntitiesEqualQuizType.size() > 0) {
                    messageService.sendMessage(getChatId(), textService.getChooseButton("testlardan"), KeyboardService.getTests(subjectEntitiesEqualQuizType));
                    user.setStep(null);
                    userRepository.update(user);
                    return;
                }
                messageService.sendMessage(getChatId(), "Test turi Attestatsiya bo'lgan birorta ham test topilmadi❗️");
                user.setStep(null);
                userRepository.update(user);
                return;
            } else if (getData().contains(Constants.BotCommand.BY_MILLIY)) {
                deleteMessage();
                List<SubjectEntity> subjectEntitiesEqualQuizType = AppUtils.getMilliySertifikatList();
                if (subjectEntitiesEqualQuizType != null && subjectEntitiesEqualQuizType.size() > 0) {
                    messageService.sendMessage(getChatId(), textService.getChooseButton("testlardan"), KeyboardService.getTests(subjectEntitiesEqualQuizType));
                    user.setStep(null);
                    userRepository.update(user);
                    return;
                }
                messageService.sendMessage(getChatId(), "Test turi Milliy sertifikat bo'lgan birorta ham test topilmadi❗️");
                user.setStep(null);
                userRepository.update(user);
                return;
            } else if (getData().contains(Constants.BotCommand.DOWNLOAD)) {
                String security_key = String.valueOf(getData().split("=")[1]);
                deleteMessage();

                Optional<SubjectEntity> optionalSubject = subjectRepository.getOne(new HashMap<>() {{
                    put("security_key", security_key);
                }}).getData();

                if (optionalSubject.isPresent()) {
                    SubjectEntity subject = optionalSubject.get();
                    List<UserAnswer> userAnswers = AppUtils.getAllUserAnswersBySubjectId(subject.getId());
                    List<AnswerEntity> answerEntityList = AppUtils.getAllAnswerBySubjectId(subject.getId());
                    StringBuilder allAnswer = new StringBuilder();

                    for (int i = 0; i < answerEntityList.size(); i++) {
                        allAnswer.append(i + 1).append(".").append(answerEntityList.get(i).getAnswer()).append("  ");
                    }

                    if (userAnswers.size() > 0) {
                        byte[] excelData = exportToExcel(userAnswers, subject, String.valueOf(allAnswer));

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
                        InputFile inputFile = new InputFile(inputStream, "Malumot.xlsx");

                        messageService.sendDocument(getChatId(), inputFile);

                        user.setStep(null);
                        userRepository.update(user);
                        messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));
                        return;
                    } else {
                        messageService.sendMessage(getChatId(), "Bu testga birorta foydalanuvchi javob bermagan❗️");
                        user.setStep(null);
                        userRepository.update(user);
                        return;
                    }

                } else {
                    _logger.error("Not found this subject by security_key!");
                }
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

    public byte[] exportToExcel(List<UserAnswer> userAnswers, SubjectEntity subject, String allAnswers) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("User Answers");

        // Sarlavhalar
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "Id", "Ism Familiya", "Fan", "Test kodi", "Test turi", "Jami savollar soni",
                "To'g'ri javoblar soni", "Noto`g`ri javoblar soni", "Ball", "Foiz",
                "Notog'ri javoblar", "Barcha javoblar", "Barcha tog'ri javoblar"
        };

        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (UserAnswer userAnswer : userAnswers) {
            Long userId = userAnswer.getUser_id();
            UserEntity userEntity = userRepository.getOne(new HashMap<>() {{
                put("id", userId);
            }}).getData().orElseThrow();

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(userAnswer.getUser_id());
            row.createCell(1).setCellValue(userEntity.getUsername());
            row.createCell(2).setCellValue(userAnswer.getSubject_name());
            row.createCell(3).setCellValue(subject.getSecurity_key());
            row.createCell(4).setCellValue(subject.getQuiz_type().getDisplayName());
            row.createCell(5).setCellValue(userAnswer.getAll_answer_count());
            row.createCell(6).setCellValue(userAnswer.getCorrect_answer_count());
            row.createCell(7).setCellValue(userAnswer.getIncorrect_answer_count());
            row.createCell(8).setCellValue(userAnswer.getBall());
            row.createCell(9).setCellValue(userAnswer.getPercentage() + "%");
            row.createCell(10).setCellValue(userAnswer.getIncorrect_answers_list());
            row.createCell(11).setCellValue(userAnswer.getAllAnswersList());
            row.createCell(12).setCellValue(allAnswers);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }

        return out.toByteArray(); // Excel faylni byte[] ko'rinishida qaytaradi
    }

}
