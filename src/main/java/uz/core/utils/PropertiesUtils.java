package uz.core.utils;

import uz.core.logger.LogManager;
import uz.db.entity.AdminEntity;
import uz.db.respository.AdminRepository;
import lombok.Getter;
import org.htmlunit.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {
    private static final LogManager _logger = new LogManager(PropertiesUtils.class);
    @Getter
    private static String telegramBaseUrl;
    @Getter
    private static String telegramBotToken;
    @Getter
    private static String telegramBotUsername;
    @Getter
    private static String telegramBotName;

    @Getter
    private static String adminUsername;



    static {
        try {
            Properties properties = new Properties();
            properties.load(FileUtils.getInstance().getPropertiesFile());

            telegramBotToken = properties.getProperty("telegram.bot.token");
            telegramBotUsername = properties.getProperty("telegram.bot.username");
            telegramBotName = properties.getProperty("telegram.bot.name");
            telegramBaseUrl = properties.getProperty("telegram.base.url");
            adminUsername = properties.getProperty("admin.username");
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }


    public static WebClient getWebClient() {
        return new WebClient();
    }

    public static List<AdminEntity> getAdmins() {
        return AdminRepository.getInstance().getList(Map.of()).getData();
    }

}
