package com.infina.fileanalyzer.service.abstracts;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling file upload operations and processing
 */
public interface IFileUploadService {

    /**
     * Processes the uploaded file and returns analysis results
     * @param uploadedFile The uploaded file
     * @return Analysis results
     */
    FileAnalysisResponseDto processUploadedFile(MultipartFile uploadedFile);

    /**
     * Processes multiple uploaded files and returns analysis results
     * @param uploadedFiles Array of uploaded files
     * @return Analysis results
     */
    FileAnalysisResponseDto processMultipleUploadedFiles(MultipartFile[] uploadedFiles);

    /**
     * Validates file type
     * @param filename The filename to check
     * @return True if supported file type
     */
    boolean isValidFileType(String filename);

    /**
     * Sanitizes the filename to make it safe
     * @param originalFilename The original filename
     * @return Sanitized filename
     */
    String sanitizeFilename(String originalFilename);
}