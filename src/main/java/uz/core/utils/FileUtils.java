package uz.core.utils;

import uz.core.logger.LogManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    @Getter
    private static final FileUtils instance = new FileUtils();
    private static final LogManager _logger = new LogManager(FileUtils.class);

    public InputStream getProperties() {
        try {
            return new FileInputStream("config/config.properties");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getPropertiesFile() {
        return instance.getProperties();
    }

    public static void saveBase64ToFile(String base64String, String filePath) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        try (OutputStream os = new FileOutputStream(filePath)) {
            os.write(decodedBytes);
        } catch (Exception e) {
            _logger.error(e.getMessage());
        }
    }

}
