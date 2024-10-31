package org.ois.plugin.utils;

import org.slf4j.Logger;

import java.io.OutputStream;

/**
 * Log Utilities
 */
public class LogUtils {

    /**
     * Get an OutputStream that will add Info log entries to the provided log
     * @param log - the log that the entries will be added to
     * @return - OutputStream that will add Info logs
     */
    public static OutputStream getRedirectOutToLogInfo(Logger log) {
        return new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();
            @Override
            public void write(int b) {
                if (b == '\n') {
                    log.info(buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }
        };
    }

    /**
     * Get an OutputStream that will add Error log entries to the provided log
     * @param log - the log that the entries will be added to
     * @return - OutputStream that will add Error logs
     */
    public static OutputStream getRedirectOutToLogErr(Logger log) {
        return new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();
            @Override
            public void write(int b) {
                if (b == '\n') {
                    log.error(buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }
        };
    }
}
