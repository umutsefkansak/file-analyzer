package com.infina.fileanalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final int MAX_THREADS = 10; // Maksimum 10 dosya desteği
    private static final String ANALYZE_THREAD_NAME_PREFIX = "FileAnalysis-";
    private static final String ARCHIVE_THREAD_NAME_PREFIX = "Archive-";

    /**
     * Dosya analizi için ExecutorService bean'i
     * Maksimum 10 dosya desteği ile cached thread pool kullanır
     */
    @Bean(name = "fileAnalysisExecutor")
    public ExecutorService fileAnalysisExecutor() {
        return Executors.newFixedThreadPool(MAX_THREADS, r -> {
            Thread thread = new Thread(r);
            thread.setName(ANALYZE_THREAD_NAME_PREFIX + thread.getId());
            thread.setDaemon(false); // Ana thread beklesin
            return thread;
        });
    }

    /**
     * Arşivleme işlemi için tek thread'li ExecutorService
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
     * Genel amaçlı cached thread pool
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