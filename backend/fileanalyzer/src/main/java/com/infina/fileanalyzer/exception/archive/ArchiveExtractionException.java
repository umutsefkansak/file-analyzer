package com.infina.fileanalyzer.exception.archive;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when extracting a ZIP archive fails.
public class ArchiveExtractionException extends FileAnalyzerException {

    public ArchiveExtractionException(String message) {
        super(message);
    }

    public ArchiveExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}