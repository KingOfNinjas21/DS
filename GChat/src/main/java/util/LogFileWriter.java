package util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class LogFileWriter extends Handler {
    // default logs file name
    private static final String DEFAULT_LOGS_FILENAME = "logs.txt";
    
    // registered loggers
    private static final Map<String, LogFileWriter> INSTANCES = new HashMap<>();
    
    // date format for persisted event logs
    private final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    // print stream to the file where logs are persisted
    private final PrintStream writer;
    
    private LogFileWriter(String filename) throws FileNotFoundException {
        writer = new PrintStream(new FileOutputStream(filename, true));
    }
    
    public static LogFileWriter getInstance(String filename) {
        if (!INSTANCES.containsKey(filename)) {
            try {
                LogFileWriter instance = new LogFileWriter(filename);
                INSTANCES.put(filename, instance);
            } catch (FileNotFoundException ex) {}
        }
        return INSTANCES.get(filename);
    }
    
    public static LogFileWriter getInstance() {
        return getInstance(DEFAULT_LOGS_FILENAME);
    }

    @Override
    public void publish(LogRecord record) {
        writer.println(df.format(new Date(record.getMillis())) + " " + record.getMessage());
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void close() throws SecurityException {
        writer.close();
    }
}
