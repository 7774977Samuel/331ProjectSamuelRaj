package project;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * FileLogger handles fault-tolerant logging with backup files.
 * If writing to the primary file fails, it attempts writing to backup files.
 * If all backups fail, it writes to a reliable fallback file: principal_log.txt
 */
public class FileLogger {

    public static final int MAXBACKUP = 5;
    private static final String FINALLOG = "principal_log.txt";
    private static final Random random = new Random();

    /**
     * Attempts to log a message to the base file and its backups on failure.
     *
     * @param baseFile the base log file name (e.g., "log.txt")
     * @param msg      the message to log
     */
    public static void log(String baseFile, String msg) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String fullMsg = "[" + time + "] " + msg;

        // Attempt writing to base log file
        if (tryWrite(baseFile, fullMsg)) return;

        // Attempt backup files
        for (int i = 1; i <= MAXBACKUP; i++) {
            String backup = baseFile.replace(".txt", i + ".txt");
            if (tryWrite(backup, fullMsg)) return;
        }

        // Final fallback
        try (FileWriter writer = new FileWriter(FINALLOG, true)) {
            writer.write(fullMsg + " [fallback log used]\n");
        } catch (IOException e) {
            System.err.println("Critical log failure: could not write to principal_log.txt.");
        }
    }

    // Attempts to write to a file, simulating 40% chance of failure
    private static boolean tryWrite(String file, String msg) {
        try {
            if (random.nextInt(100) < 40) {
                throw new IOException("Simulated failure");
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(msg + "\n");
                return true;
            }

        } catch (IOException e) {
            System.err.println("Log failed: " + file + " (" + e.getMessage() + ")");
            return false;
        }
    }
}
