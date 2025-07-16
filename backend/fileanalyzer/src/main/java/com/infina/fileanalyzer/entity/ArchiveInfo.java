package com.infina.fileanalyzer.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity class representing archive operation information.
 * Stores details of the ZIP archiving process.
 */
public class ArchiveInfo {
    private String archiveFileName;
    private String archiveFilePath;
    private List<String> archivedFileNames;
    private long archiveFileSizeBytes;
    private int archivedFileCount;
    private LocalDateTime archiveStartTime;
    private LocalDateTime archiveEndTime;
    private long archiveProcessingTimeNanos;
    private String threadName;
    private String compressionMethod;
    private double compressionRatio;

    public ArchiveInfo() {
        this.archivedFileCount = 0;
        this.archiveFileSizeBytes = 0;
        this.archiveProcessingTimeNanos = 0;
        this.compressionRatio = 0.0;
    }

    public ArchiveInfo(String archiveFileName, String archiveFilePath) {
        this();
        this.archiveFileName = archiveFileName;
        this.archiveFilePath = archiveFilePath;
    }

    public ArchiveInfo(String archiveFileName, String archiveFilePath, List<String> archivedFileNames) {
        this(archiveFileName, archiveFilePath);
        this.archivedFileNames = archivedFileNames;
        this.archivedFileCount = archivedFileNames != null ? archivedFileNames.size() : 0;
    }


    public String getArchiveFileName() {
        return archiveFileName;
    }

    public void setArchiveFileName(String archiveFileName) {
        this.archiveFileName = archiveFileName;
    }

    public String getArchiveFilePath() {
        return archiveFilePath;
    }

    public void setArchiveFilePath(String archiveFilePath) {
        this.archiveFilePath = archiveFilePath;
    }

    public List<String> getArchivedFileNames() {
        return archivedFileNames;
    }

    public void setArchivedFileNames(List<String> archivedFileNames) {
        this.archivedFileNames = archivedFileNames;
        this.archivedFileCount = archivedFileNames != null ? archivedFileNames.size() : 0;
    }

    public long getArchiveFileSizeBytes() {
        return archiveFileSizeBytes;
    }

    public void setArchiveFileSizeBytes(long archiveFileSizeBytes) {
        this.archiveFileSizeBytes = archiveFileSizeBytes;
    }

    public int getArchivedFileCount() {
        return archivedFileCount;
    }

    public void setArchivedFileCount(int archivedFileCount) {
        this.archivedFileCount = archivedFileCount;
    }

    public LocalDateTime getArchiveStartTime() {
        return archiveStartTime;
    }

    public void setArchiveStartTime(LocalDateTime archiveStartTime) {
        this.archiveStartTime = archiveStartTime;
    }

    public LocalDateTime getArchiveEndTime() {
        return archiveEndTime;
    }

    public void setArchiveEndTime(LocalDateTime archiveEndTime) {
        this.archiveEndTime = archiveEndTime;

        // Automatically calculate the duration when end time is set
        if (archiveStartTime != null) {
            this.archiveProcessingTimeNanos = java.time.Duration.between(
                    archiveStartTime, archiveEndTime).toNanos();
        }
    }

    public long getArchiveProcessingTimeNanos() {
        return archiveProcessingTimeNanos;
    }

    public void setArchiveProcessingTimeNanos(long archiveProcessingTimeNanos) {
        this.archiveProcessingTimeNanos = archiveProcessingTimeNanos;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getCompressionMethod() {
        return compressionMethod;
    }

    public void setCompressionMethod(String compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }

    public void setCompressionRatio(double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }


    public double getArchiveProcessingTimeMillis() {
        return archiveProcessingTimeNanos / 1_000_000.0;
    }

    public double getArchiveProcessingTimeSeconds() {
        return archiveProcessingTimeNanos / 1_000_000_000.0;
    }

    public double getArchiveFileSizeKB() {
        return archiveFileSizeBytes / 1024.0;
    }

    public double getArchiveFileSizeMB() {
        return archiveFileSizeBytes / (1024.0 * 1024.0);
    }


    @Override
    public String toString() {
        return "ArchiveInfo{" +
                "archiveFileName='" + archiveFileName + '\'' +
                ", archiveFilePath='" + archiveFilePath + '\'' +
                ", archivedFileCount=" + archivedFileCount +
                ", archiveFileSizeBytes=" + archiveFileSizeBytes +
                ", archiveStartTime=" + archiveStartTime +
                ", archiveEndTime=" + archiveEndTime +
                ", archiveProcessingTimeNanos=" + archiveProcessingTimeNanos +
                ", threadName='" + threadName + '\'' +
                ", compressionMethod='" + compressionMethod + '\'' +
                ", compressionRatio=" + compressionRatio +
                '}';
    }
}