package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.AnalysisResult;
import com.infina.fileanalyzer.entity.FileStats;
import com.infina.fileanalyzer.exception.file.FileNotFoundException;
import com.infina.fileanalyzer.exception.file.FileProcessingException;
import com.infina.fileanalyzer.exception.file.InvalidFileTypeException;
import com.infina.fileanalyzer.service.abstracts.IFileProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@Service
public class FileProcessingService implements IFileProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);


    /**
     * Calculates the line and character count for the given file,
     * and returns processing information via FileStats.
     * Only .txt files are allowed.
     *
     * @param filePath Path to the file to analyze
     * @return FileStats containing analysis results
     * @throws InvalidFileTypeException if file is not a .txt file
     * @throws FileNotFoundException    if file does not exist
     * @throws FileProcessingException  if an error occurs during file processing
     */
    public FileStats analyzeFile(Path filePath) {
        // Check if file exists
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File does not exist: " + filePath);
        }

        // Extension check: Only .txt files are processed.
        String fileName = filePath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".txt")) {
            throw new InvalidFileTypeException("Only .txt files can be analyzed. Found: " + fileName);
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
    private int countLines(Path filePath) {
        try (var lines = Files.lines(filePath)) {
            return (int) lines.count();
        } catch (IOException e) {
            logger.error("Error counting lines in file: {}", filePath, e);
            throw new FileProcessingException("Failed to count lines in file: " + filePath, e);
        }
    }

    // Calculates the total character count of the file
    private int countCharacters(Path filePath) {
        try {
            return Files.readString(filePath).length();
        } catch (IOException e) {
            logger.error("Error counting characters in file: {}", filePath, e);
            throw new FileProcessingException("Failed to count characters in file: " + filePath, e);
        }
    }

    // Can be used to run with thread pools
    public Callable<FileStats> analyzeFileCallable(Path filePath) {
        return () -> analyzeFile(filePath);
    }


    /**
     * Calculates the total analysis result from individual file statistics.
     * This method aggregates all individual results into a comprehensive summary.
     *
     * @param fileStatsList     List of individual file analysis results
     * @param analysisStartTime Start time of the overall analysis process
     * @return AnalysisResult containing aggregated statistics
     */
    public AnalysisResult calculateTotalResult(List<FileStats> fileStatsList, LocalDateTime analysisStartTime) {
        logger.info("Calculating total result for {} files", fileStatsList.size());
        long startTime = System.nanoTime();

        AnalysisResult result = new AnalysisResult(fileStatsList);
        result.setAnalysisStartTime(analysisStartTime);
        result.setAnalysisEndTime(LocalDateTime.now());

        // Calculate totals
        int totalLines = 0;
        int totalCharacters = 0;
        long totalProcessingTime = 0;
        int successfulFiles = 0;
        int failedFiles = 0;

        for (FileStats stats : fileStatsList) {
            if (stats.isProcessingCompleted()) {
                totalLines += stats.getLineCount();
                totalCharacters += stats.getCharacterCount();
                totalProcessingTime += stats.getProcessingTimeNanos();
                successfulFiles++;
            } else {
                failedFiles++;
            }
        }

        result.setTotalLineCount(totalLines);
        result.setTotalCharacterCount(totalCharacters);
        result.setTotalProcessingTimeNanos(totalProcessingTime);
        result.setSuccessfulFileCount(successfulFiles);
        result.setFailedFileCount(failedFiles);

        long endTime = System.nanoTime();
        long calculationDuration = endTime - startTime;

        logger.info("Total result calculation completed in {} ms ({} ns)",
                calculationDuration / 1_000_000.0, calculationDuration);
        logger.info("Total result summary: {} lines, {} characters, {} successful files, {} failed files",
                totalLines, totalCharacters, successfulFiles, failedFiles);

        return result;
    }
}