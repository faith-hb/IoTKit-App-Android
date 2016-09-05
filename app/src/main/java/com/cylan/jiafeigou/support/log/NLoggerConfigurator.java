package com.cylan.jiafeigou.support.log;


import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.File;

/**
 * Created by cylan-hunt on 16-8-17.
 */
public class NLoggerConfigurator {

    private int logLevel = Log.VERBOSE;
    private String messagePattern = "yyyy-mm-dd HH:mm:ss.SSS ";
    private String logCatPattern = "%m%n";
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + "Logger"
            + File.separator
            + Process.myPid() + ".txt";
    private int maxBackupSize = 2;
    private long maxFileSize = 512 * 1024;
    private boolean immediateFlush = true;
    private boolean useLogCatAppender = true;
    private boolean useFileAppender = true;
    private boolean resetConfiguration = true;
    private boolean internalDebugging = false;

    public int getLogLevel() {
        return logLevel;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public String getLogCatPattern() {
        return logCatPattern;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getMaxBackupSize() {
        return maxBackupSize;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public boolean isUseLogCatAppender() {
        return useLogCatAppender;
    }

    public boolean isUseFileAppender() {
        return useFileAppender;
    }

    public boolean isResetConfiguration() {
        return resetConfiguration;
    }

    public boolean isInternalDebugging() {
        return internalDebugging;
    }

    public static NLoggerConfigurator generateLoggerConfigurator() {
        return new NLoggerConfigurator();
    }

    public static class Builder {
        private NLoggerConfigurator configurator;

        public Builder() {
            configurator = new NLoggerConfigurator();
        }

        public Builder setLogLevel(int level) {
            configurator.logLevel = level;
            return this;
        }

        public Builder setFilePattern(String filePattern) {
            configurator.messagePattern = filePattern;
            return this;
        }

        public Builder setLogCatPattern(String logCatPattern) {
            configurator.logCatPattern = logCatPattern;
            return this;
        }

        public Builder setFilePath(String filePath) {
            configurator.filePath = filePath;
            return this;
        }

        public Builder setMaxBackupSize(int maxBackupSize) {
            configurator.maxBackupSize = maxBackupSize;
            return this;
        }

        public Builder setMaxFileSize(long maxFileSize) {
            configurator.maxFileSize = maxFileSize;
            return this;
        }

        public Builder setImmediateFlush(boolean immediateFlush) {
            configurator.immediateFlush = immediateFlush;
            return this;
        }

        public Builder setUseLogCatAppender(boolean useLogCatAppender) {
            configurator.useLogCatAppender = useLogCatAppender;
            return this;
        }

        public Builder setUseFileAppender(boolean useFileAppender) {
            configurator.useFileAppender = useFileAppender;
            return this;
        }

        public Builder setResetConfiguration(boolean resetConfiguration) {
            configurator.resetConfiguration = resetConfiguration;
            return this;
        }

        public Builder setInternalDebugging(boolean internalDebugging) {
            configurator.internalDebugging = internalDebugging;
            return this;
        }

        public NLoggerConfigurator build() {
            return configurator;
        }
    }
}
