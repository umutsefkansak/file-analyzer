package com.infina.fileanalyzer.exception.file;

/**
 * Base exception class for all File Analyzer application exceptions.
 * All custom exceptions in the application should extend this class.
 */
public class FileAnalyzerException extends RuntimeException {

    public FileAnalyzerException(String message) {
        super(message);
    }

    public FileAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }
}