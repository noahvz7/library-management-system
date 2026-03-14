package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// logs actions to a file for accountability
public class AuditLogger {

    private static final String LOG_FILE = "data/audit.log";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String username, String action, String target) {
        String timestamp = LocalDateTime.now().format(FORMAT);
        String entry = String.format("[%s] USER:%s ACTION:%s TARGET:%s", timestamp, username, action, target);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(entry);
        } catch (IOException e) {
            System.out.println("Warning: could not write to audit log.");
        }
    }
}
