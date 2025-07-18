package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import com.infina.fileanalyzer.entity.AnalysisResult;
import com.infina.fileanalyzer.entity.ArchiveInfo;
import com.infina.fileanalyzer.entity.FileStats;
import com.infina.fileanalyzer.service.abstracts.IFileAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Service responsible for orchestrating the file analysis process.
 * Coordinates with ThreadManagementService to analyze files, calculate results,
 * and create archives in a multi-threaded environment.
 */
@Service
public class FileAnalysisService implements IFileAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisService.class);

    private final ThreadManagementService threadManagementService;

    @Autowired
    public FileAnalysisService(ThreadManagementService threadManagementService) {
        this.threadManagementService = threadManagementService;
    }

    /**
     * Processes a list of files by analyzing their content, calculating total results,
     * and creating an archive of the processed files.
     *
     * @param filePaths List of paths to the files to be processed
     * @param inputDirectory Directory containing the input files
     * @param outputZipPath Path where the output ZIP file will be created
     * @return AnalysisResult containing the aggregated statistics of all processed files
     */
    @Override
    public FileAnalysisResponseDto processFile(List<Path> filePaths, String inputDirectory, String outputZipPath) {
        logger.info("Starting file processing for {} files from directory: {}", filePaths.size(), inputDirectory);
        LocalDateTime analysisStartTime = LocalDateTime.now();

        try {
            // Submit file analysis tasks to thread pool
            logger.debug("Submitting file analysis tasks to thread pool");
            List<Future<FileStats>> analysisFutures = threadManagementService.submitFileAnalysisTasks(filePaths);

            // Wait for all file analysis tasks to complete and collect results
            logger.debug("Waiting for file analysis tasks to complete");
            List<FileStats> fileStatsList = threadManagementService.waitForAnalysisCompletion(analysisFutures);

            // Submit total result calculation task
            logger.debug("Submitting total result calculation task");
            Future<AnalysisResult> totalResultFuture = threadManagementService.submitTotalResultCalculationTask(fileStatsList, analysisStartTime);

            // Submit archive creation task
            logger.debug("Submitting archive creation task for directory: {}", inputDirectory);
            Future<ArchiveInfo> archiveFuture = threadManagementService.submitArchiveTask(inputDirectory, outputZipPath, true);

            // Wait for total result calculation and archive creation to complete
            logger.debug("Waiting for total result calculation to complete");
            AnalysisResult totalResult = threadManagementService.waitForTotalResultCalculation(totalResultFuture);

            logger.debug("Waiting for archive creation to complete");
            ArchiveInfo archiveInfo = threadManagementService.waitForArchiveCompletion(archiveFuture);

            // Log thread pool status after all operations
            threadManagementService.logThreadPoolStatus();

            // Create and return the combined DTO
            FileAnalysisResponseDto responseDto = new FileAnalysisResponseDto(totalResult, archiveInfo);

            logger.info("File processing completed successfully. Processed {} files, created archive: {}",
                    totalResult.getTotalProcessedFiles(), archiveInfo.getArchiveFileName());

            return responseDto;

        } catch (Exception e) {
            logger.error("Error during file processing", e);
            throw new RuntimeException("File processing failed", e);
        }
    }
}