package uz.telegram.handler;

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
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.MessageService;
import uz.telegram.service.TextService;

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
                if (getText().length() == allAnswerBySubjectId.size()) {
                    Map<String, Object> result = check(getText(), allAnswerBySubjectId);
                    int correctCount = (int) result.get("correctCount");
                    int incorrectCount = (int) result.get("incorrectCount");
                    double totalScore = (double) result.get("totalScore");
                    double accuracyPercentage = (double) result.get("accuracyPercentage");

                    String textServiceResult = textService.getResult(security_key, subjectEntity.getName(), allAnswerBySubjectId.size(), correctCount, incorrectCount, accuracyPercentage, totalScore, subjectEntity.getQuiz_type());
                    messageService.sendMessage(getChatId(), textServiceResult, KeyboardService.getMainKeyboard(user));
                    messageService.sendMessageToAdmin(user, PropertiesUtils.getAdmins(), textServiceResult);
                    user.setCurrent_security_key(null);
                    userRepository.update(user);
                    return;
                } else {
                    messageService.sendMessage(getChatId(), textService.errorSendAnswerCount(security_key, getText().length(), allAnswerBySubjectId.size()), KeyboardService.getMainKeyboard(user));
                    return;
                }

            } else {
                messageService.sendMessage(getChatId(), textService.subjectNotFound(), KeyboardService.getMainKeyboard(user));
                user.setCurrent_security_key(null);
                userRepository.update(user);
                return;
            }

        }

        if (PropertiesUtils.getAdmins().stream().
                anyMatch(adminEntity -> adminEntity.getId().equals(user.getId()))) {
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

                    if (firstIndex != -1 && secondIndex != -1
                        && firstIndex + 1 < getText().length()
                        && secondIndex + 1 < getText().length()) {

                        String subject_name = getText().substring(firstIndex + 1, secondIndex);
                        String answers = getText().substring(secondIndex + 1);

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
                    String answers = getText().substring(secondIndex + 1);

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


            }
        }

        if (user.getStep() == null) {
//            if (!getText().startsWith("test") && getText().contains("*")) {
//                String security_key = getText().substring(0, getText().indexOf('*'));
//                Optional<SubjectEntity> optionalSubject = subjectRepository.getOne(new HashMap<>() {{
//                    put("security_key", security_key);
//                }}).getData();
//
//                if (optionalSubject.isPresent()) {
//                    SubjectEntity subjectEntity = optionalSubject.get();
//                    int starIndex = getText().indexOf('*');
//                    if (starIndex != -1 && starIndex < getText().length() - 1) {
//                        String getAnswer = getText().substring(starIndex + 1).replaceAll("[^a-zA-Z]", "");
//                        List<AnswerEntity> allAnswerBySubjectId = AppUtils.getAllAnswerBySubjectId(subjectEntity.getId());
//                        if (getAnswer.length() == allAnswerBySubjectId.size()) {
//                            Map<String, Object> result = check(getAnswer, allAnswerBySubjectId);
//                            int correctCount = (int) result.get("correctCount");
//                            int incorrectCount = (int) result.get("incorrectCount");
//                            double totalScore = (double) result.get("totalScore");
//                            double accuracyPercentage = (double) result.get("accuracyPercentage");
//
//                            String textServiceResult = textService.getResult(security_key, subjectEntity.getName(), allAnswerBySubjectId.size(), correctCount, incorrectCount, accuracyPercentage, totalScore, subjectEntity.getQuiz_type());
//                            messageService.sendMessage(getChatId(), textServiceResult, KeyboardService.getMainKeyboard(user));
//                            messageService.sendMessageToAdmin(user, PropertiesUtils.getAdmins(), textServiceResult);
//                            return;
//                        } else {
//                            messageService.sendMessage(getChatId(), textService.errorSendAnswerCount(security_key, getAnswer.length(), allAnswerBySubjectId.size()), KeyboardService.getMainKeyboard(user));
//                            return;
//                        }
//                    } else {
//                        messageService.sendMessage(getChatId(), textService.subjectNotFound(), KeyboardService.getMainKeyboard(user));
//                        return;
//                    }
//                } else {
//                    messageService.sendMessage(getChatId(), textService.subjectNotFound(), KeyboardService.getMainKeyboard(user));
//                    return;
//                }
//            }
            messageService.sendMessage(getChatId(), "<b>\uD83D\uDC4BAssalomu alaykum\n\nBotimizga xush kelibsiz botimz faqat maxsus link orqali kirganda ishlaydi iltimos maxsus link orqali kiring va testlaringizni javobini yuboring</b>", KeyboardService.getMainKeyboard(user));
        }

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
                answer.setScore(1.1F);
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

        for (int i = 0; i < allAnswerBySubjectId.size(); i++) {
            char userChar = userAnswer.charAt(i);
            String correctAnswer = allAnswerBySubjectId.get(i).getAnswer();
            float score = allAnswerBySubjectId.get(i).getScore();

            if (String.valueOf(userChar).equals(correctAnswer)) {
                correctCount++;
                totalScore += score;
            } else {
                incorrectCount++;
            }
        }

        double accuracyPercentage = ((double) correctCount / allAnswerBySubjectId.size()) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("correctCount", correctCount);
        result.put("incorrectCount", incorrectCount);
        result.put("totalScore", totalScore);
        result.put("accuracyPercentage", accuracyPercentage);

        return result;
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

    public Long getChatId() {
        return message.getChatId();
    }

    public String getText() {
        return message.getText() == null ? "" : message.getText();
    }
}
