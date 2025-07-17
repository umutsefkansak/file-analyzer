package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.FileStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class FileProcessingService {

    /**
     * Calculates the line and character count for the given file,
     * and returns processing information via FileStats.
     * Only .txt files are allowed.
     */
    public FileStats analyzeFile(Path filePath) throws IOException {
        // Extension check: Only .txt files are processed.
        String fileName = filePath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Only .txt files can be analyzed.");
        }

        FileStats stats = new FileStats();
        stats.setFileName(fileName);
        stats.setThreadName(Thread.currentThread().getName());
        stats.setProcessingStartTime(LocalDateTime.now());

        // Calculate line and character count
        int lineCount = countLines(filePath);
        int characterCount = countCharacters(filePath);

        stats.setLineCount(lineCount);
        stats.setCharacterCount(characterCount);

        stats.setProcessingEndTime(LocalDateTime.now());
        stats.setProcessingCompleted(true);

        return stats;
    }

    // Calculates the line count of the file
    private int countLines(Path filePath) throws IOException {
        try (var lines = Files.lines(filePath)) {
            return (int) lines.count();
        }
    }

    // Calculates the total character count of the file
    private int countCharacters(Path filePath) throws IOException {
        return Files.readString(filePath).length();
    }

    // Can be used to run with thread pools
    public Callable<FileStats> analyzeFileCallable(Path filePath) {
        return () -> analyzeFile(filePath);
    }
}
