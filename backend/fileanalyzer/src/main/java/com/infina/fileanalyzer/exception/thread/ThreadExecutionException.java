package com.infina.fileanalyzer.exception.thread;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when thread execution fails.
public class ThreadExecutionException extends FileAnalyzerException {

    public ThreadExecutionException(String message) {
        super(message);
    }

    public ThreadExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}