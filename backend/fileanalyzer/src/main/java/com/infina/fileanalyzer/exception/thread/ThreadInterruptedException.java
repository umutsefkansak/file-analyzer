package com.infina.fileanalyzer.exception.thread;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when thread execution is interrupted.
public class ThreadInterruptedException extends FileAnalyzerException {

    public ThreadInterruptedException(String message) {
        super(message);
    }

    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}