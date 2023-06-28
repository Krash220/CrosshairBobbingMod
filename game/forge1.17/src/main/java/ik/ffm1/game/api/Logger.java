package ik.ffm1.game.api;

import org.slf4j.LoggerFactory;

public class Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("${MOD_ID}");

    public static void info(String log, Object... args) {
        LOGGER.info(log, args);
    }
}
