package uz.core.utils;

import uz.core.logger.LogManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    @Getter
    private static final FileUtils instance = new FileUtils();
    private  final LogManager _logger = new LogManager(FileUtils.class);

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


}
