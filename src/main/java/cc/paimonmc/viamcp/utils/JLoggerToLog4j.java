package cc.paimonmc.viamcp.utils;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JLoggerToLog4j extends Logger {
    private final org.apache.logging.log4j.Logger base;

    public JLoggerToLog4j(final org.apache.logging.log4j.Logger logger) {
        super("logger", null);
        this.base = logger;
    }

    public void log(final LogRecord record) {
        this.log(record.getLevel(), record.getMessage());
    }

    public void log(final Level level, final String msg) {
        if (level == Level.FINE) {
            this.base.debug(msg);
        } else if (level == Level.WARNING) {
            this.base.warn(msg);
        } else if (level == Level.SEVERE) {
            this.base.error(msg);
        } else if (level == Level.INFO) {
            this.base.info(msg);
        } else {
            this.base.trace(msg);
        }
    }

    public void log(final Level level, final String msg, final Object param1) {
        if (level == Level.FINE) {
            this.base.debug(msg, param1);
        } else if (level == Level.WARNING) {
            this.base.warn(msg, param1);
        } else if (level == Level.SEVERE) {
            this.base.error(msg, param1);
        } else if (level == Level.INFO) {
            this.base.info(msg, param1);
        } else {
            this.base.trace(msg, param1);
        }
    }

    public void log(final Level level, final String msg, final Object[] params) {
        log(level, MessageFormat.format(msg, params));
    }

    public void log(final Level level, final String msg, final Throwable params) {
        if (level == Level.FINE) {
            this.base.debug(msg, params);
        } else if (level == Level.WARNING) {
            this.base.warn(msg, params);
        } else if (level == Level.SEVERE) {
            this.base.error(msg, params);
        } else if (level == Level.INFO) {
            this.base.info(msg, params);
        } else {
            this.base.trace(msg, params);
        }
    }
}
