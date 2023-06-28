package ik.ffm1.game.api;

import org.apache.logging.log4j.LogManager;

public class Logger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("${MOD_ID}");

    public static void info(String log, Object... args) {
        LOGGER.info(log, args);
    }
}
