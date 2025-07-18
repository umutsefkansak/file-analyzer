package com.infina.fileanalyzer.exception.file;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when a required file is not found.
public class FileNotFoundException extends FileAnalyzerException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}