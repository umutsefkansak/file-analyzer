package com.infina.fileanalyzer.service.abstracts;

import com.infina.fileanalyzer.entity.AnalysisResult;
import com.infina.fileanalyzer.entity.ArchiveInfo;
import com.infina.fileanalyzer.entity.FileStats;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Interface for thread management service that determines which thread pool will execute which operations.
 * Provides methods for submitting tasks to different thread pools and waiting for their completion.
 */
public interface IThreadManagementService {
    /**
     * Submits multiple file analysis tasks to the file analysis thread pool.
     * Each file is processed by a separate thread from the pool.
     *
     * @param filePaths List of file paths to analyze
     * @return List of Future objects for tracking completion
     */
    List<Future<FileStats>> submitFileAnalysisTasks(List<Path> filePaths);

    /**
     * Submits archive creation task to the archive thread pool.
     * Archive operation is performed by a single dedicated thread.
     *
     * @param inputDirectory Directory containing files to archive
     * @param outputZipPath Path where the ZIP file will be created
     * @param deleteSourceFiles Whether to delete source files after archiving
     * @return Future object for tracking completion
     */
    Future<ArchiveInfo> submitArchiveTask(String inputDirectory, String outputZipPath, boolean deleteSourceFiles);

    /**
     * Submits total result calculation task to the general thread pool.
     * This method calculates the overall analysis results using a separate thread.
     *
     * @param fileStatsList List of individual file analysis results
     * @param analysisStartTime Start time of the overall analysis process
     * @return Future object for tracking completion
     */
    Future<AnalysisResult> submitTotalResultCalculationTask(List<FileStats> fileStatsList, LocalDateTime analysisStartTime);

    /**
     * Waits for total result calculation to complete and returns the result.
     * Provides detailed logging about calculation completion.
     *
     * @param calculationFuture Future object from total result calculation task
     * @return AnalysisResult containing aggregated statistics
     */
    AnalysisResult waitForTotalResultCalculation(Future<AnalysisResult> calculationFuture);

    /**
     * Waits for all file analysis tasks to complete and collects results.
     * Provides detailed logging about completion status and timing.
     *
     * @param futures List of Future objects from file analysis tasks
     * @return List of FileStats results
     */
    List<FileStats> waitForAnalysisCompletion(List<Future<FileStats>> futures);

    /**
     * Waits for archive task to complete and returns the result.
     * Provides detailed logging about archive operation completion.
     *
     * @param archiveFuture Future object from archive task
     * @return ArchiveInfo result
     */
    ArchiveInfo waitForArchiveCompletion(Future<ArchiveInfo> archiveFuture);

    /**
     * Logs thread pool status and resource usage information.
     * Useful for monitoring and debugging thread pool utilization.
     */
    void logThreadPoolStatus();

    /**
     * Shuts down thread pools.
     * Should be called when application is shutting down.
     */
    void shutdown();
}
