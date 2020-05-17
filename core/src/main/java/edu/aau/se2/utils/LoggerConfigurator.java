package edu.aau.se2.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface LoggerConfigurator {

    static Logger getConfiguredLogger(String tag, Level level) {
        Logger log = Logger.getLogger(tag);
        if (log.getHandlers().length == 0) {
            Handler handlerObj = new ConsoleHandler();
            handlerObj.setLevel(level);
            log.addHandler(handlerObj);
        }
        log.setLevel(level);
        log.setUseParentHandlers(false);

        return log;
    }
}
