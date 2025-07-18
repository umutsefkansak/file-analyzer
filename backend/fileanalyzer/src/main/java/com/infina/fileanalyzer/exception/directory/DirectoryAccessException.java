package com.infina.fileanalyzer.exception.directory;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when directory creation or access fails.
public class DirectoryAccessException extends FileAnalyzerException {

    public DirectoryAccessException(String message) {
        super(message);
    }

    public DirectoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}