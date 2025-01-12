package uz.core.logger;


import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class LogManager extends StreamHandler {
    private static final LogFormatter formatter = new LogFormatter();
    private final Class<?> targetClass;

    public LogManager(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public void publish(LogRecord record) {
        record.setSourceClassName(targetClass.getName());
        System.out.println(formatter.format(record));
    }


    public void info(String message) {
        this.publish(new LogRecord(Level.INFO, message));
    }

    public void warning(String message) {
        this.publish(new LogRecord(Level.WARNING, message));
    }

    public void error(String message) {
        this.publish(new LogRecord(Level.SEVERE, message));
    }


}
