package lab1.utils;

public class LoggerHelper {
    public final static int LOG_LEVEL_DEBUG = 0;
    public final static int LOG_LEVEL_INFO = 1;

    private static boolean isDebug() {
        String debug = System.getenv("DEBUG");
        debug = debug == null ? "" : debug.toLowerCase();

        return debug.equals("true");
    }

    public static void log(String message, int level) {
        if (level == LOG_LEVEL_DEBUG && !isDebug()) {
            return;
        }
        System.out.println(message);
    }

    public static void log(String message) {
        log(message, LOG_LEVEL_INFO);
    }
}
