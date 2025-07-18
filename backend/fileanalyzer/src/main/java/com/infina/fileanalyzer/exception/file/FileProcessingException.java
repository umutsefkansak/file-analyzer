package com.infina.fileanalyzer.exception.file;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when processing a file fails.
public class FileProcessingException extends FileAnalyzerException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}