package com.infina.fileanalyzer.exception.file;

import com.infina.fileanalyzer.exception.file.FileAnalyzerException;

// Exception thrown when a file has an unsupported type.
public class InvalidFileTypeException extends FileAnalyzerException {

    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}