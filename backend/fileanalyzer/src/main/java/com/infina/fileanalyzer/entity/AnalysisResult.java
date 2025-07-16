package com.infina.fileanalyzer.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity class representing the overall result of all file analysis operations.
 * Summarizes the analysis results of multiple files.
 */
public class AnalysisResult {
    private List<FileStats> fileStatsList;
    private int totalLineCount;
    private int totalCharacterCount;
    private int totalProcessedFiles;
    private long totalProcessingTimeNanos;
    private LocalDateTime analysisStartTime;
    private LocalDateTime analysisEndTime;
    private int failedFileCount;
    private int successfulFileCount;

    public AnalysisResult() {
        this.failedFileCount = 0;
        this.successfulFileCount = 0;
        this.totalLineCount = 0;
        this.totalCharacterCount = 0;
        this.totalProcessedFiles = 0;
        this.totalProcessingTimeNanos = 0;
    }

    public AnalysisResult(List<FileStats> fileStatsList) {
        this();
        this.fileStatsList = fileStatsList;
        this.totalProcessedFiles = fileStatsList != null ? fileStatsList.size() : 0;
    }

    public List<FileStats> getFileStatsList() {
        return fileStatsList;
    }

    public void setFileStatsList(List<FileStats> fileStatsList) {
        this.fileStatsList = fileStatsList;
        this.totalProcessedFiles = fileStatsList != null ? fileStatsList.size() : 0;
    }

    public int getTotalLineCount() {
        return totalLineCount;
    }

    public void setTotalLineCount(int totalLineCount) {
        this.totalLineCount = totalLineCount;
    }

    public int getTotalCharacterCount() {
        return totalCharacterCount;
    }

    public void setTotalCharacterCount(int totalCharacterCount) {
        this.totalCharacterCount = totalCharacterCount;
    }

    public int getTotalProcessedFiles() {
        return totalProcessedFiles;
    }

    public void setTotalProcessedFiles(int totalProcessedFiles) {
        this.totalProcessedFiles = totalProcessedFiles;
    }

    public long getTotalProcessingTimeNanos() {
        return totalProcessingTimeNanos;
    }

    public void setTotalProcessingTimeNanos(long totalProcessingTimeNanos) {
        this.totalProcessingTimeNanos = totalProcessingTimeNanos;
    }

    public LocalDateTime getAnalysisStartTime() {
        return analysisStartTime;
    }

    public void setAnalysisStartTime(LocalDateTime analysisStartTime) {
        this.analysisStartTime = analysisStartTime;
    }

    public LocalDateTime getAnalysisEndTime() {
        return analysisEndTime;
    }

    public void setAnalysisEndTime(LocalDateTime analysisEndTime) {
        this.analysisEndTime = analysisEndTime;

        // Automatically calculate the total duration when end time is set
        if (analysisStartTime != null) {
            this.totalProcessingTimeNanos = java.time.Duration.between(
                    analysisStartTime, analysisEndTime).toNanos();
        }
    }

    public int getFailedFileCount() {
        return failedFileCount;
    }

    public void setFailedFileCount(int failedFileCount) {
        this.failedFileCount = failedFileCount;
    }

    public int getSuccessfulFileCount() {
        return successfulFileCount;
    }

    public void setSuccessfulFileCount(int successfulFileCount) {
        this.successfulFileCount = successfulFileCount;
    }

    public double getTotalProcessingTimeMillis() {
        return totalProcessingTimeNanos / 1_000_000.0;
    }

    public double getTotalProcessingTimeSeconds() {
        return totalProcessingTimeNanos / 1_000_000_000.0;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "totalLineCount=" + totalLineCount +
                ", totalCharacterCount=" + totalCharacterCount +
                ", totalProcessedFiles=" + totalProcessedFiles +
                ", totalProcessingTimeNanos=" + totalProcessingTimeNanos +
                ", analysisStartTime=" + analysisStartTime +
                ", analysisEndTime=" + analysisEndTime +
                ", failedFileCount=" + failedFileCount +
                ", successfulFileCount=" + successfulFileCount +
                '}';
    }
}