package com.infina.fileanalyzer.service.abstracts;

import com.infina.fileanalyzer.entity.FileStats;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;


/**
 * Interface for file processing and analysis operations
 */
public interface IFileProcessingService {
    /**
     * Analyzes a file to calculate line and character counts
     * Only .txt files are allowed for analysis
     * @param filePath Path to the file to be analyzed
     * @return FileStats containing analysis results and processing information
     * @throws IOException if an I/O error occurs during file reading
     * @throws IllegalArgumentException if the file is not a .txt file
     */
    FileStats analyzeFile(Path filePath) throws IOException;

    /**
     * Returns a Callable that can be used with thread pools for file analysis
     * @param filePath Path to the file to be analyzed
     * @return Callable that returns FileStats when executed
     */
    Callable<FileStats> analyzeFileCallable(Path filePath);
}
