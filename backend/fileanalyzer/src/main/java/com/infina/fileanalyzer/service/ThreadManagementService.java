package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.AnalysisResult;
import com.infina.fileanalyzer.entity.ArchiveInfo;
import com.infina.fileanalyzer.entity.FileStats;
import com.infina.fileanalyzer.exception.thread.ThreadExecutionException;
import com.infina.fileanalyzer.exception.thread.ThreadInterruptedException;
import com.infina.fileanalyzer.service.abstracts.IThreadManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of ThreadManagementService that determines which thread pool will execute which operations.
 * Provides logging and timing calculations for all operations.
 */
@Service
public class ThreadManagementService implements IThreadManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ThreadManagementService.class);

    private final ExecutorService fileAnalysisExecutor;
    private final ExecutorService archiveExecutor;
    private final ExecutorService generalExecutor;
    private final FileProcessingService fileProcessingService;
    private final ArchiveService archiveService;

    @Autowired
    public ThreadManagementService(
            @Qualifier("fileAnalysisExecutor") ExecutorService fileAnalysisExecutor,
            @Qualifier("archiveExecutor") ExecutorService archiveExecutor,
            @Qualifier("generalExecutor") ExecutorService generalExecutor,
            FileProcessingService fileProcessingService,
            ArchiveService archiveService) {
        this.fileAnalysisExecutor = fileAnalysisExecutor;
        this.archiveExecutor = archiveExecutor;
        this.generalExecutor = generalExecutor;
        this.fileProcessingService = fileProcessingService;
        this.archiveService = archiveService;
    }

    /**
     * Submits multiple file analysis tasks to the file analysis thread pool.
     * Each file is processed by a separate thread from the pool.
     *
     * @param filePaths List of file paths to analyze
     * @return List of Future objects for tracking completion
     * @throws ThreadExecutionException if task submission fails
     * @throws ThreadInterruptedException if thread is interrupted during execution
     */
    public List<Future<FileStats>> submitFileAnalysisTasks(List<Path> filePaths) {
        logger.info("Starting file analysis task submission for {} files", filePaths.size());
        long startTime = System.nanoTime();
        LocalDateTime startDateTime = LocalDateTime.now();

        // Create callable tasks for each file
        List<Callable<FileStats>> analysisTasks = filePaths.stream()
                .map(filePath -> {
                    logger.debug("Creating analysis task for file: {}", filePath.getFileName());
                    return fileProcessingService.analyzeFileCallable(filePath);
                })
                .toList();

        List<Future<FileStats>> futures;
        try {
            // Submit all tasks to the file analysis thread pool
            futures = fileAnalysisExecutor.invokeAll(analysisTasks);

            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;

            logger.info("File analysis task submission completed in {} ms ({} ns)",
                    durationNanos / 1_000_000.0, durationNanos);
            logger.info("Submitted {} analysis tasks to thread pool at {}",
                    futures.size(), startDateTime);

            // Log individual thread assignments
            for (int i = 0; i < filePaths.size(); i++) {
                logger.debug("File {} assigned to analysis thread pool",
                        filePaths.get(i).getFileName());
            }

        } catch (InterruptedException e) {
            logger.error("File analysis task submission was interrupted", e);
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException("File analysis task submission was interrupted", e);
        }

        return futures;
    }

    /**
     * Submits archive creation task to the archive thread pool.
     * Archive operation is performed by a single dedicated thread.
     *
     * @param inputDirectory Directory containing files to archive
     * @param outputZipPath Path where the ZIP file will be created
     * @param deleteSourceFiles Whether to delete source files after archiving
     * @return Future object for tracking completion
     * @throws ThreadExecutionException if task submission fails
     */
    public Future<ArchiveInfo> submitArchiveTask(String inputDirectory, String outputZipPath, boolean deleteSourceFiles) {
        logger.info("Starting archive task submission for directory: {}", inputDirectory);
        long startTime = System.nanoTime();
        LocalDateTime startDateTime = LocalDateTime.now();

        // Create callable task for archiving
        Callable<ArchiveInfo> archiveTask = () -> {
            logger.debug("Archive task starting execution in thread: {}",
                    Thread.currentThread().getName());

            // Create archive
            ArchiveInfo archiveInfo = archiveService.createArchive(inputDirectory, outputZipPath);

            // Delete source files if requested and archiving was successful
            if (deleteSourceFiles && archiveInfo.getArchivedFileCount() > 0) {
                try {
                    List<Path> txtFiles = archiveService.findTxtFiles(inputDirectory);
                    archiveService.deleteSourceFiles(txtFiles);
                    logger.info("Source files deleted after successful archiving: {} files", txtFiles.size());
                } catch (Exception e) {
                    logger.error("Failed to delete source files after archiving", e);
                }
            }

            return archiveInfo;
        };

        Future<ArchiveInfo> future;
        try {
            // Submit archive task to the single-threaded archive executor
            future = archiveExecutor.submit(archiveTask);

            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;

            logger.info("Archive task submission completed in {} ms ({} ns)",
                    durationNanos / 1_000_000.0, durationNanos);
            logger.info("Archive task submitted to archive thread pool at {}", startDateTime);
            logger.debug("Archive operation will be executed by: archive thread pool");

        } catch (Exception e) {
            logger.error("Archive task submission failed", e);
            throw new ThreadExecutionException("Archive task submission failed", e);
        }

        return future;
    }

    /**
     * Submits total result calculation task to the general thread pool.
     * This method calculates the overall analysis results using a separate thread.
     *
     * @param fileStatsList List of individual file analysis results
     * @param analysisStartTime Start time of the overall analysis process
     * @return Future object for tracking completion
     * @throws ThreadExecutionException if task submission fails
     */
    public Future<AnalysisResult> submitTotalResultCalculationTask(List<FileStats> fileStatsList, LocalDateTime analysisStartTime) {
        logger.info("Starting total result calculation task submission for {} files", fileStatsList.size());
        long startTime = System.nanoTime();
        LocalDateTime startDateTime = LocalDateTime.now();

        // Create callable task for total result calculation
        Callable<AnalysisResult> calculationTask = () -> {
            logger.debug("Total result calculation task starting execution in thread: {}",
                    Thread.currentThread().getName());

            return fileProcessingService.calculateTotalResult(fileStatsList, analysisStartTime);
        };

        Future<AnalysisResult> future;
        try {
            // Submit calculation task to the general thread pool
            future = generalExecutor.submit(calculationTask);

            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;

            logger.info("Total result calculation task submission completed in {} ms ({} ns)",
                    durationNanos / 1_000_000.0, durationNanos);
            logger.info("Total result calculation task submitted to general thread pool at {}", startDateTime);
            logger.debug("Total result calculation will be executed by: general thread pool");

        } catch (Exception e) {
            logger.error("Total result calculation task submission failed", e);
            throw new ThreadExecutionException("Total result calculation task submission failed", e);
        }

        return future;
    }


    /**
     * Waits for total result calculation to complete and returns the result.
     * Provides detailed logging about calculation completion.
     *
     * @param calculationFuture Future object from total result calculation task
     * @return AnalysisResult containing aggregated statistics
     * @throws ThreadInterruptedException if thread is interrupted during wait
     * @throws ThreadExecutionException if execution fails
     */
    public AnalysisResult waitForTotalResultCalculation(Future<AnalysisResult> calculationFuture) {
        logger.info("Waiting for total result calculation completion");
        long startTime = System.nanoTime();

        AnalysisResult result;
        try {
            logger.debug("Blocking wait for total result calculation completion");
            result = calculationFuture.get(); // Blocking wait

            long endTime = System.nanoTime();
            long waitDurationNanos = endTime - startTime;

            logger.info("Total result calculation completed successfully:");
            logger.info("- Total files processed: {}", result.getTotalProcessedFiles());
            logger.info("- Successful files: {}", result.getSuccessfulFileCount());
            logger.info("- Failed files: {}", result.getFailedFileCount());
            logger.info("- Total lines: {}", result.getTotalLineCount());
            logger.info("- Total characters: {}", result.getTotalCharacterCount());
            logger.info("- Total processing time: {} ms", result.getTotalProcessingTimeMillis());
            logger.info("- Analysis duration: {} seconds", result.getTotalProcessingTimeSeconds());
            logger.info("- Wait time for calculation completion: {} ms ({} ns)",
                    waitDurationNanos / 1_000_000.0, waitDurationNanos);

        } catch (InterruptedException e) {
            logger.error("Total result calculation was interrupted", e);
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException("Total result calculation was interrupted", e);
        } catch (ExecutionException e) {
            logger.error("Total result calculation failed during execution", e);
            throw new ThreadExecutionException("Total result calculation execution failed", e);
        }

        return result;
    }

    /**
     * Waits for all file analysis tasks to complete and collects results.
     * Provides detailed logging about completion status and timing.
     *
     * @param futures List of Future objects from file analysis tasks
     * @return List of FileStats results
     * @throws ThreadInterruptedException if thread is interrupted during wait
     * @throws ThreadExecutionException if execution fails
     */
    public List<FileStats> waitForAnalysisCompletion(List<Future<FileStats>> futures) {
        logger.info("Waiting for completion of {} file analysis tasks", futures.size());
        long startTime = System.nanoTime();
        LocalDateTime startDateTime = LocalDateTime.now();

        List<FileStats> results = new CopyOnWriteArrayList<>();
        int completedCount = 0;
        int failedCount = 0;

        for (int i = 0; i < futures.size(); i++) {
            Future<FileStats> future = futures.get(i);
            try {
                logger.debug("Waiting for analysis task {} to complete", i + 1);
                FileStats result = future.get(); // Blocking wait
                results.add(result);
                completedCount++;

                logger.debug("Analysis task {} completed successfully. File: {}, Thread: {}, Duration: {} ms",
                        i + 1, result.getFileName(), result.getThreadName(),
                        result.getProcessingTimeMillis());

            } catch (InterruptedException e) {
                logger.error("Analysis task {} was interrupted", i + 1, e);
                Thread.currentThread().interrupt();
                failedCount++;
                throw new ThreadInterruptedException("Analysis task was interrupted", e);
            } catch (ExecutionException e) {
                logger.error("Analysis task {} failed during execution", i + 1, e);
                failedCount++;
                throw new ThreadExecutionException("Analysis task execution failed", e);
            }
        }

        long endTime = System.nanoTime();
        long totalDurationNanos = endTime - startTime;

        logger.info("File analysis completion summary:");
        logger.info("- Total tasks: {}", futures.size());
        logger.info("- Completed successfully: {}", completedCount);
        logger.info("- Failed: {}", failedCount);
        logger.info("- Total wait time: {} ms ({} ns)",
                totalDurationNanos / 1_000_000.0, totalDurationNanos);
        logger.info("- Analysis completion finished at: {}", LocalDateTime.now());

        return results;
    }

    /**
     * Waits for archive task to complete and returns the result.
     * Provides detailed logging about archive operation completion.
     *
     * @param archiveFuture Future object from archive task
     * @return ArchiveInfo result
     * @throws ThreadInterruptedException if thread is interrupted during wait
     * @throws ThreadExecutionException if execution fails
     */
    public ArchiveInfo waitForArchiveCompletion(Future<ArchiveInfo> archiveFuture) {
        logger.info("Waiting for archive task completion");
        long startTime = System.nanoTime();
        LocalDateTime startDateTime = LocalDateTime.now();

        ArchiveInfo result;
        try {
            logger.debug("Blocking wait for archive task completion");
            result = archiveFuture.get(); // Blocking wait

            long endTime = System.nanoTime();
            long waitDurationNanos = endTime - startTime;

            logger.info("Archive task completed successfully:");
            logger.info("- Archive file: {}", result.getArchiveFileName());
            logger.info("- Archive path: {}", result.getArchiveFilePath());
            logger.info("- Files archived: {}", result.getArchivedFileCount());
            logger.info("- Archive size: {} KB ({} bytes)",
                    result.getArchiveFileSizeKB(), result.getArchiveFileSizeBytes());
            logger.info("- Archive thread: {}", result.getThreadName());
            logger.info("- Archive processing time: {} ms", result.getArchiveProcessingTimeMillis());
            logger.info("- Wait time for archive completion: {} ms ({} ns)",
                    waitDurationNanos / 1_000_000.0, waitDurationNanos);

        } catch (InterruptedException e) {
            logger.error("Archive task was interrupted", e);
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException("Archive task was interrupted", e);
        } catch (ExecutionException e) {
            logger.error("Archive task failed during execution", e);
            throw new ThreadExecutionException("Archive task execution failed", e);
        }

        return result;
    }

    /**
     * Logs thread pool status and resource usage information.
     * Useful for monitoring and debugging thread pool utilization.
     */
    public void logThreadPoolStatus() {
        logger.info("Thread Pool Status Report:");

        // Analysis thread pool status
        if (fileAnalysisExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor analysisPool = (ThreadPoolExecutor) fileAnalysisExecutor;
            logger.info("File Analysis Thread Pool:");
            logger.info("- Core pool size: {}", analysisPool.getCorePoolSize());
            logger.info("- Maximum pool size: {}", analysisPool.getMaximumPoolSize());
            logger.info("- Current pool size: {}", analysisPool.getPoolSize());
            logger.info("- Active threads: {}", analysisPool.getActiveCount());
            logger.info("- Completed tasks: {}", analysisPool.getCompletedTaskCount());
            logger.info("- Total tasks: {}", analysisPool.getTaskCount());
        }

        // Archive thread pool status
        if (archiveExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor archivePool = (ThreadPoolExecutor) archiveExecutor;
            logger.info("Archive Thread Pool:");
            logger.info("- Core pool size: {}", archivePool.getCorePoolSize());
            logger.info("- Maximum pool size: {}", archivePool.getMaximumPoolSize());
            logger.info("- Current pool size: {}", archivePool.getPoolSize());
            logger.info("- Active threads: {}", archivePool.getActiveCount());
            logger.info("- Completed tasks: {}", archivePool.getCompletedTaskCount());
            logger.info("- Total tasks: {}", archivePool.getTaskCount());
        }

        // General thread pool status
        if (generalExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor generalPool = (ThreadPoolExecutor) generalExecutor;
            logger.info("General Thread Pool:");
            logger.info("- Core pool size: {}", generalPool.getCorePoolSize());
            logger.info("- Maximum pool size: {}", generalPool.getMaximumPoolSize());
            logger.info("- Current pool size: {}", generalPool.getPoolSize());
            logger.info("- Active threads: {}", generalPool.getActiveCount());
            logger.info("- Completed tasks: {}", generalPool.getCompletedTaskCount());
            logger.info("- Total tasks: {}", generalPool.getTaskCount());
        }
    }

    /**
     * Shuts down thread pools gracefully.
     * Should be called when application is shutting down.
     *
     * @throws ThreadInterruptedException if shutdown is interrupted
     */
    public void shutdown() {
        logger.info("Shutting down thread pools...");

        fileAnalysisExecutor.shutdown();
        archiveExecutor.shutdown();
        generalExecutor.shutdown();

        try {
            if (!fileAnalysisExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("File analysis thread pool did not terminate gracefully, forcing shutdown");
                fileAnalysisExecutor.shutdownNow();
            }

            if (!archiveExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Archive thread pool did not terminate gracefully, forcing shutdown");
                archiveExecutor.shutdownNow();
            }

            if (!generalExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("General thread pool did not terminate gracefully, forcing shutdown");
                generalExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown was interrupted", e);
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException("Thread pool shutdown was interrupted", e);
        }

        logger.info("All thread pools have been shut down");
    }
}