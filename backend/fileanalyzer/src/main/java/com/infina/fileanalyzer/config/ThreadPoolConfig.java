package com.infina.fileanalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final int MAX_THREADS = 10; // Supports up to 10 files
    private static final String ANALYZE_THREAD_NAME_PREFIX = "FileAnalysis-";
    private static final String ARCHIVE_THREAD_NAME_PREFIX = "Archive-";

    /**
     * ExecutorService bean for file analysis
     * Uses a fixed thread pool with support for up to 10 files
     */
    @Bean(name = "fileAnalysisExecutor")
    public ExecutorService fileAnalysisExecutor() {
        return Executors.newFixedThreadPool(MAX_THREADS, r -> {
            Thread thread = new Thread(r);
            thread.setName(ANALYZE_THREAD_NAME_PREFIX + thread.getId());
            thread.setDaemon(false); // Let the main thread wait
            return thread;
        });
    }

    /**
     * Single-threaded ExecutorService for archiving operations
     */
    @Bean(name = "archiveExecutor")
    public ExecutorService archiveExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(ARCHIVE_THREAD_NAME_PREFIX+ thread.getId());
            thread.setDaemon(false);
            return thread;
        });
    }

    /**
     * General-purpose cached thread pool
     */
    @Bean(name = "generalExecutor")
    public ExecutorService generalExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("General-" + thread.getId());
            thread.setDaemon(false);
            return thread;
        });
    }
}