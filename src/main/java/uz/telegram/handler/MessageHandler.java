package uz.telegram.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import uz.core.Constants;
import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import uz.core.utils.AppUtils;
import uz.core.utils.PropertiesUtils;
import uz.db.entity.*;
import uz.db.enums.QuizType;
import uz.db.respository.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.db.service.CertificateService;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.MessageService;
import uz.telegram.service.TextService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static uz.core.Constants.BotCommand.*;

public class MessageHandler {
    private final LogManager _logger = new LogManager(MessageHandler.class);
    private final UserEntity user;
    private final Message message;
    private final MessageService messageService;
    private final UserRepository userRepository = UserRepository.getInstance();
    private static final ChannelRepository channelRepository = ChannelRepository.getInstance();
    private static final AdminRepository adminRepository = AdminRepository.getInstance();
    private static final AnswerRepository answerRepository = AnswerRepository.getInstance();
    private static final SubjectRepository subjectRepository = SubjectRepository.getInstance();
    private static final CertificateService certificateService = CertificateService.getInstance();
    private ResourceBundle rb;
    private TextService textService;

    public MessageHandler(UserEntity user, Message message, ResourceBundle rb) {
        this.user = user;
        this.rb = rb;
        this.textService = new TextService(rb);
        this.message = message;
        this.messageService = new MessageService(user);
    }

    public void handler() {
        if (user.getStatus_id() == null) {
            messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));
            return;
        }

        if (getText().equals("/start")) {
            messageService.sendMessage(getChatId(), "<b>\uD83D\uDC4BAssalomu alaykum\n\nBotimizga xush kelibsiz botimz faqat maxsus link orqali kirganda ishlaydi iltimos maxsus link orqali kiring va testlaringizni javobini yuboring</b>", KeyboardService.getMainKeyboard(user));
            user.setStep(null);
            user.setCurrent_security_key(null);
            userRepository.update(user);
            return;
        }

        if (getText().equals(Constants.BotCommand.INFO)) {
            messageService.sendMessage(getChatId(), textService.getInfoButton());
            return;
        }

        if (user.getCurrent_security_key() != null) {
            String security_key = user.getCurrent_security_key();
            if (getText().startsWith("/start ")) {
                String key = getText().split(" ")[1];
                user.setCurrent_security_key(key);
                userRepository.update(user);
                return;
            }
            Optional<SubjectEntity> optionalSubject = subjectRepository.getOne(new HashMap<>() {{
                put("security_key", security_key);
            }}).getData();

            if (optionalSubject.isPresent()) {
                SubjectEntity subjectEntity = optionalSubject.get();
                List<AnswerEntity> allAnswerBySubjectId = AppUtils.getAllAnswerBySubjectId(subjectEntity.getId());
                String answers = getText().replaceAll("[0-9]", "");

                if (answers.length() == allAnswerBySubjectId.size()) {
                    Map<String, Object> result = check(answers, allAnswerBySubjectId);
                    int correctCount = (int) result.get("correctCount");
                    int incorrectCount = (int) result.get("incorrectCount");
                    double totalScore = (double) result.get("totalScore");
                    List<String> incorrectAnswers = (List<String>) result.get("incorrectAnswers");
                    double accuracyPercentage = (double) result.get("accuracyPercentage");

                    String textServiceResult = textService.getResult(security_key, subjectEntity.getName(), allAnswerBySubjectId.size(), correctCount, incorrectCount, accuracyPercentage, totalScore, incorrectAnswers, subjectEntity.getQuiz_type());
                    messageService.sendMessage(getChatId(), textServiceResult, KeyboardService.getMainKeyboard(user));
                    messageService.sendMessageToAdmin(user, PropertiesUtils.getAdmins(), textServiceResult, answers);
                    Message message = messageService.sendMessage(getChatId(), "<b>⏬Sertifikat yuklanmoqda...</b>", true);

                    if (subjectEntity.getQuiz_type().equals(QuizType.ATTESTATSIYA)) {
                        SertificatAttestatsiyaEntity sertificatAttestatsiyaEntity = new SertificatAttestatsiyaEntity();
                        sertificatAttestatsiyaEntity.setFio(user.getUsername());
                        if (totalScore >= 60 && totalScore < 70) {
                            sertificatAttestatsiyaEntity.setSort("Toifa 2");
                        } else if (totalScore >= 70 && totalScore < 80) {
                            sertificatAttestatsiyaEntity.setSort("Toifa 1");
                        } else if (totalScore >= 80) {
                            sertificatAttestatsiyaEntity.setSort("Olit toifa");
                        } else if (totalScore < 60) {
                            sertificatAttestatsiyaEntity.setSort("-");
                        }

                        if (totalScore >= 86) {
                            sertificatAttestatsiyaEntity.setFor70Score((float) totalScore);
                        } else {
                            sertificatAttestatsiyaEntity.setFor70Score(0F);
                        }
                        sertificatAttestatsiyaEntity.setOverallScore(Float.valueOf(String.format("%.1f", totalScore)));

                        String base64Certificate = certificateService.getAttestatsiyaCertificate(sertificatAttestatsiyaEntity);

                        if (base64Certificate.equals("")) {
                            messageService.sendMessage(getChatId(), "Xatolik yuz berdi❗\uFE0F");
                            user.setCurrent_security_key(null);
                            userRepository.update(user);
                            return;
                        }
                        deleteMessage(user.getId(), message);
                        sendPhotoFromJson(base64Certificate);
                    } else if (subjectEntity.getQuiz_type().equals(QuizType.MILLIY_SERTIFIKAT)) {
                        SertificatTestCheckerMilliyDto testCheckerMilliyDto = new SertificatTestCheckerMilliyDto();
                        Map<String, Object> calculate = calculate(answers, allAnswerBySubjectId);
                        testCheckerMilliyDto.setPart_1((float) calculate.get("1-12"));
                        testCheckerMilliyDto.setPart_2((float) calculate.get("13-17"));
                        testCheckerMilliyDto.setPart_3((float) calculate.get("18-22"));
                        testCheckerMilliyDto.setPart_4((float) calculate.get("33-35"));
                        testCheckerMilliyDto.setFio(user.getUsername());
                        testCheckerMilliyDto.setOverallScore(testCheckerMilliyDto.getPart_1() + testCheckerMilliyDto.getPart_2() + testCheckerMilliyDto.getPart_3() + testCheckerMilliyDto.getPart_4());

                        String base64Certificate = certificateService.getMilliyCertificate(testCheckerMilliyDto);
                        if (base64Certificate.equals("")) {
                            messageService.sendMessage(getChatId(), "Xatolik yuz berdi❗\uFE0F");
                            user.setCurrent_security_key(null);
                            userRepository.update(user);
                            return;
                        }

                        deleteMessage(user.getId(), message);
                        sendPhotoFromJson(base64Certificate);
                    }

                    user.setCurrent_security_key(null);
                    userRepository.update(user);
                    return;
                } else {
                    messageService.sendMessage(getChatId(), textService.errorSendAnswerCount(security_key, answers.length(), allAnswerBySubjectId.size()), KeyboardService.getMainKeyboard(user));
                    return;
                }

            } else {
                messageService.sendMessage(getChatId(), textService.subjectNotFound(), KeyboardService.getMainKeyboard(user));
                user.setCurrent_security_key(null);
                userRepository.update(user);
                return;
            }

        }

        if (PropertiesUtils.getAdmins().stream().anyMatch(adminEntity -> adminEntity.getId().equals(user.getId()))) {
            if (user.getStep() == null)
                switch (getText()) {
                    case Constants.BotCommand.BUTTON_STATISTIC -> {
                        messageService.sendMessage(getChatId(), textService.getStatistic(AppUtils.getAllStatistic()), KeyboardService.backButton(CALL_MAIN_MENU));
                        return;
                    }
                    case Constants.BotCommand.BUTTON_CHANNEL -> {
                        DDLResponse<List<ChannelEntity>> response = channelRepository.getList(Map.of());
                        if (!response.getStatus()) {
                            messageService.sendError(getChatId(), response);
                            return;
                        }

                        messageService.sendMessage(getChatId(), textService.getChooseButton("Kannallardan"), KeyboardService.getChannels(response.getData()));
                        return;
                    }
                    case Constants.BotCommand.BUTTON_ADMIN -> {
                        DDLResponse<List<AdminEntity>> response = adminRepository.getList(Map.of());
                        if (!response.getStatus()) {
                            messageService.sendError(getChatId(), response);
                            return;
                        }

                        messageService.sendMessage(getChatId(), textService.getChooseButton("Adminlardan"), KeyboardService.getAdmins(response.getData()));
                        return;
                    }
                    case Constants.BotCommand.BUTTON_SEND_MESSAGE -> {
                        user.setStep(Constants.BotCommand.CALL_SEND_MESSAGE);
                        userRepository.update(user);
                        messageService.sendMessage(getChatId(), textService.enterData("Xabarni"), KeyboardService.backButton(CALL_MAIN_MENU));
                        return;
                    }
                    case Constants.BotCommand.CREATE_TEST -> {
                        messageService.sendMessage(getChatId(), textService.getChooseButton("Test turlarini"), KeyboardService.getQuizType());
                        return;
                    }
                    case Constants.BotCommand.INFO -> {
                        messageService.sendMessage(getChatId(), textService.getInfoButton());
                        return;
                    }

                    case Constants.BotCommand.MY_TESTS -> {
                        DDLResponse<List<SubjectEntity>> response = subjectRepository.getList(Map.of());
                        if (response.getData().size() == 0) {
                            messageService.sendMessage(getChatId(), "Birorta ham test mavjud emas❗️");
                            return;
                        }
                        messageService.sendMessage(getChatId(), textService.getChooseButton("qidiruv turlaridan"), KeyboardService.getFilter());
                        return;
                    }

                }

            if (user.getStep() != null) {
                if (user.getStep().equals(CALL_SEND_MESSAGE)) {
                    SenderEntity sender = AppUtils.getSender();

                    if (sender == null) {
                        messageService.sendMessage(getChatId(), Constants.ResponseMessage.SYSTEM_ERROR, KeyboardService.backButton(CALL_MAIN_MENU));
                        return;
                    }

                    if (sender.getSendStatus().equalsIgnoreCase("true")) {
                        messageService.sendMessage(getChatId(), "Xabar yubora olmaysiz boshqa xabar yuborilmoqda!", KeyboardService.getMainKeyboard(user));
                        user.setStep(null);
                        userRepository.update(user);
                        return;
                    }

                    sender.setMessageId(Long.valueOf(message.getMessageId()));
                    sender.setSendStatus("true");
                    sender.setAdmin_id(getChatId());
                    sender.setSendCount(0L);
                    sender.setNotSendUser(0L);
                    sender.setSendUser(0L);
                    sender.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

                    AppUtils.updateSender(sender);

                    messageService.sendMessage(getChatId(), TEXT_DOING);
                    messageService.sendMessage(getChatId(), textService.startMessage(), KeyboardService.getMainKeyboard(user));

                    user.setStep(null);
                    userRepository.update(user);
                }

                if (user.getStep().equals(Constants.BotCommand.ADD_CHANNEL)) {
                    long channelId;
                    try {
                        channelId = Long.parseLong(getText());
                    } catch (NumberFormatException e) {
                        _logger.error(e.getMessage());
                        messageService.sendMessage(getChatId(), Constants.ResponseMessage.PLEASE_ENTER_ONLY_NUMBER);
                        return;
                    }

                    ChannelEntity entity = new ChannelEntity();
                    entity.setId(channelId);
                    entity.setCreated_users_id(user.getId());
                    DDLResponse<ChannelEntity> insertResponse = channelRepository.insert(entity);
                    if (!insertResponse.getStatus()) {
                        messageService.sendError(getChatId(), insertResponse);
                        return;
                    }

                    DDLResponse<List<ChannelEntity>> response = channelRepository.getList(Map.of());
                    if (!response.getStatus()) {
                        messageService.sendError(getChatId(), response);
                        return;
                    }
                    user.setStep(null);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.getChooseButton("Kannallardan"), KeyboardService.getChannels(response.getData()));
                }

                if (user.getStep().equals(Constants.BotCommand.ADD_ADMIN)) {
                    long adminId;
                    try {
                        adminId = Long.parseLong(getText());
                    } catch (NumberFormatException e) {
                        _logger.error(e.getMessage());
                        messageService.sendMessage(getChatId(), Constants.ResponseMessage.PLEASE_ENTER_ONLY_NUMBER);
                        return;
                    }

                    AdminEntity entity = new AdminEntity();
                    entity.setId(adminId);
                    entity.setCreated_users_id(user.getId());
                    DDLResponse<AdminEntity> insertResponse = adminRepository.insert(entity);
                    if (!insertResponse.getStatus()) {
                        messageService.sendError(getChatId(), insertResponse);
                        return;
                    }

                    DDLResponse<List<AdminEntity>> response = adminRepository.getList(Map.of());
                    if (!response.getStatus()) {
                        messageService.sendError(getChatId(), response);
                        return;
                    }
                    user.setStep(null);
                    userRepository.update(user);
                    messageService.sendMessage(getChatId(), textService.getChooseButton("Adminlardan"), KeyboardService.getAdmins(response.getData()));
                }
                if (user.getStep().equals(Constants.BotCommand.ATTESTATSIYA)) {
                    int firstIndex = getText().indexOf("*");
                    int secondIndex = getText().indexOf("*", firstIndex + 1);
                    String answers = getText().substring(secondIndex + 1).replaceAll("[0-9]", "");

                    if (firstIndex != -1 && secondIndex != -1
                        && firstIndex + 1 < getText().length()
                        && secondIndex + 1 < getText().length()) {

                        String subject_name = getText().substring(firstIndex + 1, secondIndex);

                        SubjectEntity savedSubject = createSubject(getChatId(), subject_name, QuizType.ATTESTATSIYA);
                        SubjectEntity subjectEntity = subjectRepository.getOne(new HashMap<>() {{
                            put("security_key", savedSubject.getSecurity_key());
                        }}).getData().orElseThrow();

                        createAnswers(getChatId(), subjectEntity.getId(), answers, subjectEntity.getQuiz_type());
                        messageService.sendMessage(getChatId(), textService.createTestSuccess(savedSubject, answers.length(), user.getUsername()));
                        user.setStep(null);
                        userRepository.update(user);
                        return;
                    } else {
                        messageService.sendMessage(getChatId(), textService.errorCreateTest(""), KeyboardService.backButton(CALL_MAIN_MENU));
                        return;
                    }
                }
                if (user.getStep().equals(Constants.BotCommand.MILLIY_SERTIFIKAT)) {
                    int firstIndex = getText().indexOf("*");
                    int secondIndex = getText().indexOf("*", firstIndex + 1);
                    String answers = getText().substring(secondIndex + 1).replaceAll("[0-9]", "");

                    if (firstIndex != -1 && secondIndex != -1
                        && firstIndex + 1 < getText().length()
                        && secondIndex + 1 < getText().length() && answers.length() == 35) {

                        String subject_name = getText().substring(firstIndex + 1, secondIndex);

                        SubjectEntity savedSubject = createSubject(getChatId(), subject_name, QuizType.MILLIY_SERTIFIKAT);
                        SubjectEntity subjectEntity = subjectRepository.getOne(new HashMap<>() {{
                            put("security_key", savedSubject.getSecurity_key());
                        }}).getData().orElseThrow();

                        createAnswers(getChatId(), subjectEntity.getId(), answers, QuizType.MILLIY_SERTIFIKAT);
                        messageService.sendMessage(getChatId(), textService.createTestSuccess(savedSubject, answers.length(), user.getUsername()));
                        user.setStep(null);
                        userRepository.update(user);
                        return;
                    } else {
                        messageService.sendMessage(getChatId(), textService.errorCreateTest("\nEslatma❗️Milliy sertifikat uchun javoblar 35 ta bo'lishi kerak"), KeyboardService.backButton(CALL_MAIN_MENU));
                        return;
                    }
                }
                if (user.getStep().equals(SHOW_BY_SUBJECT_NAME)) {
                    deleteMessage();
                    List<SubjectEntity> subjectEntityList = AppUtils.getSubjectNameList(getText());
                    if (subjectEntityList != null && subjectEntityList.size() > 0) {
                        messageService.sendMessage(getChatId(), textService.getChooseButton("testlardan"), KeyboardService.getTests(subjectEntityList));
                        user.setStep(null);
                        userRepository.update(user);
                        return;
                    }
                    messageService.sendMessage(getChatId(), "Bu nomli birorta ham test topilmadi❗️");
                    user.setStep(null);
                    userRepository.update(user);
                    return;
                }


            }
        }

        if (user.getStep() == null) {
            messageService.sendMessage(getChatId(), "<b>\uD83D\uDC4BAssalomu alaykum\n\nBotimizga xush kelibsiz botimz faqat maxsus link orqali kirganda ishlaydi iltimos maxsus link orqali kiring va testlaringizni javobini yuboring</b>", KeyboardService.getMainKeyboard(user));
        }

    }

    private Map<String, Object> calculate(String answer, List<AnswerEntity> allAnswerBySubjectId) {
        float totalscore1To12 = 0;
        float totalscore13To17 = 0;
        float totalscore18To22 = 0;
        float totalscore33To35 = 0;

        for (int i = 0; i < allAnswerBySubjectId.size(); i++) {
            char userChar = answer.charAt(i);
            String correctAnswer = allAnswerBySubjectId.get(i).getAnswer();
            float score = allAnswerBySubjectId.get(i).getScore();

            if (i > 0 && i <= 12) {
                if (String.valueOf(userChar).equals(correctAnswer)) {
                    totalscore1To12 += score;
                }
            }
            if (i > 12 && i <= 17) {
                if (String.valueOf(userChar).equals(correctAnswer)) {
                    totalscore13To17 += score;
                }
            }
            if (i > 17 && i <= 22) {
                if (String.valueOf(userChar).equals(correctAnswer)) {
                    totalscore18To22 += score;
                }
            }
            if (i > 33 && i <= 35) {
                if (String.valueOf(userChar).equals(correctAnswer)) {
                    totalscore33To35 += score;
                }
            }

        }

        Map<String, Object> result = new HashMap<>();
        result.put("1-12", totalscore1To12);
        result.put("13-17", totalscore13To17);
        result.put("18-22", totalscore18To22);
        result.put("33-35", totalscore33To35);

        return result;
    }

    private void createAnswers(Long createdUserId, Long subjectId, String answers, QuizType quizType) {
        List<AnswerEntity> answerEntities = new ArrayList<>();

        List<Float> milliyBallar = Arrays.asList(
                1.1F, 1.1F, 1.1F, 1.7F, 1.1F, 1.1F, 1.7F, 2.5F, 1.7F, 1.7F,
                1.7F, 2.5F, 1.7F, 1.7F, 1.7F, 1.7F, 1.7F, 1.7F, 1.7F, 1.7F,
                1.7F, 1.7F, 1.1F, 1.1F, 1.1F, 1.1F, 1.1F, 2.5F, 2.5F, 2.5F,
                2.5F, 2.5F, 1.7F, 1.7F, 1.7F
        );

        for (int i = 0; i < answers.length(); i++) {
            char answerChar = answers.charAt(i);

            AnswerEntity answer = new AnswerEntity();
            answer.setCreator_user_id(createdUserId);
            answer.setSubject_id(subjectId);
            answer.setAnswer(String.valueOf(answerChar));

            if (quizType.getDisplayName().equals(QuizType.ATTESTATSIYA.name())) {
                answer.setScore(2F);
            } else if (quizType.getDisplayName().equals(QuizType.MILLIY_SERTIFIKAT.name())) {
                answer.setScore(milliyBallar.get(i));
            }
            answerEntities.add(answer);
        }
        saveAnswers(answerEntities);
    }


    private void saveAnswers(List<AnswerEntity> answers) {
        for (AnswerEntity answer : answers) {
            answerRepository.insert(answer);
        }
    }

    private SubjectEntity createSubject(Long id, String subjectName, QuizType quizType) {
        SubjectEntity subjectEntity = new SubjectEntity();
        subjectEntity.setCreated_user_id(id);
        subjectEntity.setName(subjectName);
        subjectEntity.setQuiz_type(quizType);
        subjectEntity.setSecurity_key(UUID.randomUUID().toString());
        DDLResponse<SubjectEntity> insert = subjectRepository.insert(subjectEntity);
        return insert.getData();
    }


    public Map<String, Object> check(String userAnswer, List<AnswerEntity> allAnswerBySubjectId) {
        int correctCount = 0;
        int incorrectCount = 0;
        double totalScore = 0;

        List<String> incorrectAnswers = new ArrayList<>();

        for (int i = 0; i < allAnswerBySubjectId.size(); i++) {
            char userChar = userAnswer.charAt(i);
            String correctAnswer = allAnswerBySubjectId.get(i).getAnswer();
            float score = allAnswerBySubjectId.get(i).getScore();

            if (String.valueOf(userChar).equals(correctAnswer)) {
                correctCount++;
                totalScore += score;
            } else {
                incorrectCount++;
                incorrectAnswers.add((i + 1) + ". " + userChar + "  ");
            }
        }

        double accuracyPercentage = ((double) correctCount / allAnswerBySubjectId.size()) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("correctCount", correctCount);
        result.put("incorrectCount", incorrectCount);
        result.put("totalScore", totalScore);
        result.put("accuracyPercentage", accuracyPercentage);
        result.put("incorrectAnswers", incorrectAnswers);

        return result;
    }


    public void sendPhotoFromJson(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            String base64Image = rootNode.path("sertificat").asText();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
            InputStream inputStream = new ByteArrayInputStream(decodedBytes);

            messageService.sendPhoto(getChatId(), inputStream);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }


    public void deleteMessage() {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();

            deleteMessage.setChatId(getChatId());
            deleteMessage.setMessageId(message.getMessageId());

            BaseTelegramBot.getSender().execute(deleteMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public void deleteMessage(Long chatId, Message message) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();

            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());

            BaseTelegramBot.getSender().execute(deleteMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public Long getChatId() {
        return message.getChatId();
    }

    public String getText() {
        return message.getText() == null ? "" : message.getText();
    }
}
