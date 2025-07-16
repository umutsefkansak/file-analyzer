package com.infina.fileanalyzer.entity;

import java.time.LocalDateTime;

/**
 * Entity class representing file analysis results.
 * Holds line count, character count, and processing information for each file.
 */
public class FileStats {
    private String fileName;
    private int lineCount;
    private int characterCount;
    private long processingTimeNanos;
    private LocalDateTime processingStartTime;
    private LocalDateTime processingEndTime;
    private String threadName;
    private boolean processingCompleted;

    public FileStats() {
        this.processingCompleted = false;
        //this.processingStartTime = LocalDateTime.now();
    }

    public FileStats(String fileName, int lineCount, int characterCount) {
        this();
        this.fileName = fileName;
        this.lineCount = lineCount;
        this.characterCount = characterCount;
        this.processingCompleted = true;
    }


    public FileStats(String fileName, int lineCount, int characterCount,
                     long processingTimeNanos, String threadName) {
        this(fileName, lineCount, characterCount);
        this.processingTimeNanos = processingTimeNanos;
        this.threadName = threadName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public long getProcessingTimeNanos() {
        return processingTimeNanos;
    }

    public void setProcessingTimeNanos(long processingTimeNanos) {
        this.processingTimeNanos = processingTimeNanos;
    }

    public LocalDateTime getProcessingStartTime() {
        return processingStartTime;
    }

    public void setProcessingStartTime(LocalDateTime processingStartTime) {
        this.processingStartTime = processingStartTime;
    }

    public LocalDateTime getProcessingEndTime() {
        return processingEndTime;
    }

    public void setProcessingEndTime(LocalDateTime processingEndTime) {
        this.processingEndTime = processingEndTime;

        // Automatically calculate the duration when end time is set
        if (processingStartTime != null) {
            this.processingTimeNanos = java.time.Duration.between(
                    processingStartTime, processingEndTime).toNanos();
        }
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public boolean isProcessingCompleted() {
        return processingCompleted;
    }

    public void setProcessingCompleted(boolean processingCompleted) {
        this.processingCompleted = processingCompleted;
    }


    public double getProcessingTimeMillis() {
        return processingTimeNanos / 1_000_000.0;
    }

    @Override
    public String toString() {
        return "FileStats{" +
                "fileName='" + fileName + '\'' +
                ", lineCount=" + lineCount +
                ", characterCount=" + characterCount +
                ", processingTimeNanos=" + processingTimeNanos +
                ", processingStartTime=" + processingStartTime +
                ", processingEndTime=" + processingEndTime +
                ", threadName='" + threadName + '\'' +
                ", processingCompleted=" + processingCompleted +
                '}';
    }
}
