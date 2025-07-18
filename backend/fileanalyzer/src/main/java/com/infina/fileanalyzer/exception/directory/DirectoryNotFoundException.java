package com.infina.fileanalyzer.exception.directory;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when a required directory is not found.
public class DirectoryNotFoundException extends FileAnalyzerException {

    public DirectoryNotFoundException(String message) {
        super(message);
    }

    public DirectoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}