package uz.core.logger;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String YELLOW = "\033[0;33m";

    @Override
    public String format(LogRecord record) {
        Level level = record.getLevel();
        LocalDateTime date = LocalDateTime.now();
        String color;

        switch (level.getName()) {
            case "SEVERE":
                color = RED;
                break;
            case "WARNING":
                color = YELLOW;
                break;
            case "INFO":
            default:
                color = GREEN;
                break;
        }

        return String.format("%s%s [%s] %s: %s%s",
                color,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                level,
                record.getSourceClassName(),
                formatMessage(record),
                RESET
        );
    }
}
