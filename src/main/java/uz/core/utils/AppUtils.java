package uz.core.utils;

import org.apache.ibatis.session.SqlSession;
import uz.core.Constants;
import uz.core.base.entity.DDLResponse;
import uz.core.logger.LogManager;
import uz.db.entity.*;
import uz.db.enums.QuizType;
import uz.db.respository.ChannelRepository;
import uz.db.entity.SenderEntity;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.htmlunit.websocket.client.Main;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import uz.telegram.core.BaseTelegramBot;
import uz.telegram.service.KeyboardService;
import uz.telegram.service.TextService;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class AppUtils {
    private static final LogManager _logger = new LogManager(AppUtils.class);
    private static final ChannelRepository channelRepository = ChannelRepository.getInstance();

    private static SqlSessionFactory getSqlSession() {
        try {
            InputStream stream = Resources.getResourceAsStream(Main.class.getClassLoader(), "db/mybatis.xml");
            return new SqlSessionFactoryBuilder().build(stream);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean isJoinedChannel(Long id, TextService textService) {
        DDLResponse<List<ChannelEntity>> response = channelRepository.getList(Map.of());

        if (response.getStatus()) {
            try {
                List<String> targetChannel = new ArrayList<>();

                response.getData().forEach(channelEntity -> {
                    String channelId = channelEntity.getId().toString();

                    if (!channelId.startsWith("-100")) {
                        channelId = "-100" + channelId;
                    }

                    GetChatMember getChatMember = new GetChatMember();

                    getChatMember.setChatId(channelId);
                    getChatMember.setUserId(id);

                    try {
                        ChatMember member = BaseTelegramBot.getSender().execute(getChatMember);

                        if (!member.getStatus().equalsIgnoreCase("member")
                            && !member.getStatus().equalsIgnoreCase("administrator")
                            && !member.getStatus().equalsIgnoreCase("creator")
                        ) {
                            GetChat getChat = new GetChat();
                            getChat.setChatId(channelId);

                            Chat chat = BaseTelegramBot.getSender().execute(getChat);
                            targetChannel.add(chat.getInviteLink());
                        }
                    } catch (Exception e) {
                        _logger.error("isJoinedChannel: " + e.getMessage());
                    }
                });

                if (!targetChannel.isEmpty()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(id);
                    sendMessage.setText(textService.getChooseChannelText());
                    sendMessage.setParseMode(Constants.ParseMode.HTML);
                    sendMessage.setReplyMarkup(KeyboardService.getChannelButton(targetChannel, textService));
                    BaseTelegramBot.getSender().execute(sendMessage);
                    return false;
                }

                return true;
            } catch (Exception e) {
                _logger.error("isJoinedChannel: " + e.getMessage());
                return true;
            }
        }
        _logger.error("isJoinedChannel: " + response.getLogMessage());
        return true;
    }

    public static List<AnswerEntity> getAllAnswerBySubjectId(Long subject_id) {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectAnswerBySubjectId", subject_id);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<SubjectEntity> getSubjectByCreatedAdminId(Long created_user_id) {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectSubjectCreatedByAdminId", created_user_id);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static AllStatisticEntity getAllStatistic() {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectOne("selectAllStatistic");
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return new AllStatisticEntity();
        }
    }

    public static SenderEntity getSender() {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectOne("selectSender");
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return null;
        }
    }

    public static void updateSender(SenderEntity sender) {
        try (SqlSession session = getSqlSession().openSession()) {
            session.update("updateSender", parse(sender));
            session.commit();
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

    public static Map<String, Object> parse(Object entity) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = entity.getClass();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    map.put(field.getName(), field.get(entity));
                } catch (Exception e) {
                    _logger.error(e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }

        return map;
    }

    public static List<SubjectEntity> getAttestatsiyaList() {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectAttestatsiya", QuizType.ATTESTATSIYA);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return null;
        }
    }

    public static List<UserAnswer> getAllUserAnswersBySubjectId(Long subject_id) {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectAllUserAnswersBySubjectId", subject_id);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<SubjectEntity> getMilliySertifikatList() {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectMilliy", QuizType.MILLIY_SERTIFIKAT);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return null;
        }
    }

    public static List<SubjectEntity> getSubjectNameList(String name) {
        try (SqlSession session = getSqlSession().openSession()) {
            return session.selectList("selectSubjectsByName", name);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return null;
        }
    }

}
