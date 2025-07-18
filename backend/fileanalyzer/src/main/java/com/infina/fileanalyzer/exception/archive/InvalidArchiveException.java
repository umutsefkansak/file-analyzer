package com.infina.fileanalyzer.exception.archive;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when an archive format is invalid.
public class InvalidArchiveException extends FileAnalyzerException {

    public InvalidArchiveException(String message) {
        super(message);
    }

    public InvalidArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}