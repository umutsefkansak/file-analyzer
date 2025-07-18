package com.infina.fileanalyzer.exception.archive;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when creating a ZIP archive fails.
public class ArchiveCreationException extends FileAnalyzerException {

    public ArchiveCreationException(String message) {
        super(message);
    }

    public ArchiveCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}