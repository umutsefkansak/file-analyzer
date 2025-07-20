package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;

import com.infina.fileanalyzer.exception.directory.DirectoryAccessException;
import com.infina.fileanalyzer.exception.file.FileProcessingException;
import com.infina.fileanalyzer.exception.status.NoContentException;
import com.infina.fileanalyzer.service.abstracts.IFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileUploadService implements IFileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final FileAnalysisService fileAnalysisService;

    @Value("${file.analyzer.input.directory}")
    private String inputDirectory;

    public FileUploadService(FileAnalysisService fileAnalysisService) {
        this.fileAnalysisService = fileAnalysisService;
    }

    /**
     * Processes the uploaded file and returns analysis results
     * @param uploadedFile The uploaded file
     * @return Analysis results
     */
    public FileAnalysisResponseDto processUploadedFile(MultipartFile uploadedFile) {
        if (uploadedFile == null || uploadedFile.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty or null");
        }

        // Prepare input directory
        Path inputDir = Paths.get(inputDirectory);
        prepareInputDirectory(inputDir);

        String originalFilename = uploadedFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileProcessingException("File name is null");
        }

        logger.info("Processing uploaded file: {}", originalFilename);

        try {
            // Process based on file type
            if (originalFilename.toLowerCase().endsWith(".txt")) {
                // Direct TXT file
                processTxtFile(uploadedFile, inputDir);
            } else if (originalFilename.toLowerCase().endsWith(".zip")) {
                // ZIP file - extract TXT files inside
                processZipFile(uploadedFile, inputDir);
            }else {
                throw new FileProcessingException("Unsupported file type: " + originalFilename);
            }

            // Now call the existing analysis method
            return fileAnalysisService.analyzeAllFiles();

        } catch (Exception e) {
            logger.error("Error processing uploaded file: {}", originalFilename, e);
            throw new FileProcessingException("Failed to process uploaded file: " + e.getMessage(), e);
        }
    }


    public FileAnalysisResponseDto processMultipleUploadedFiles(MultipartFile[] uploadedFiles) {
        if (uploadedFiles == null || uploadedFiles.length == 0) {
            throw new FileProcessingException("No files uploaded");
        }

        // Total file size validation
        long totalSize = 0;
        for (MultipartFile file : uploadedFiles) {
            if (file != null && !file.isEmpty()) {
                totalSize += file.getSize();
            }
        }



        Path inputDir = Paths.get(inputDirectory);
        prepareInputDirectory(inputDir);

        logger.info("Processing {} uploaded files", uploadedFiles.length);

        int processedFileCount = 0;
        try {
            for (MultipartFile file : uploadedFiles) {
                if (file == null || file.isEmpty()) {
                    logger.warn("Skipping empty file");
                    continue;
                }


                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    logger.warn("Skipping file with null name");
                    continue;
                }

                logger.info("Processing file: {}", originalFilename);

                if (originalFilename.toLowerCase().endsWith(".txt")) {
                    processTxtFile(file, inputDir);
                    processedFileCount++;
                } else if (originalFilename.toLowerCase().endsWith(".zip")) {
                    processZipFile(file, inputDir);
                    processedFileCount++;
                } else {
                    logger.warn("Skipping unsupported file type: {}", originalFilename);
                }
            }

            if (processedFileCount == 0) {
                throw new NoContentException("No valid files were processed");
            }

            logger.info("Successfully processed {} files", processedFileCount);
            return fileAnalysisService.analyzeAllFiles();

        } catch (Exception e) {
            logger.error("Error processing multiple uploaded files", e);
            throw new FileProcessingException("Failed to process multiple files: " + e.getMessage(), e);
        }
    }

    /**
     * Copies the TXT file to the input directory
     * @param file The uploaded TXT file
     * @param inputDir The input directory
     */
    private void processTxtFile(MultipartFile file, Path inputDir) throws IOException {
        String filename = file.getOriginalFilename();
        Path targetPath = inputDir.resolve(filename);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("TXT file saved to input directory: {}", filename);
        }
    }

    /**
     * Extracts TXT files from the ZIP archive to the input directory
     * @param file The uploaded ZIP file
     * @param inputDir The input directory
     */
    private void processZipFile(MultipartFile file, Path inputDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            int extractedCount = 0;

            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".txt")) {
                    String filename = Paths.get(entry.getName()).getFileName().toString();
                    Path targetPath = inputDir.resolve(filename);

                    // Security check: Prevent path traversal attacks
                    if (!targetPath.startsWith(inputDir)) {
                        logger.warn("Skipping potentially dangerous file path: {}", entry.getName());
                        continue;
                    }

                    try (OutputStream out = Files.newOutputStream(targetPath)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        extractedCount++;
                        logger.debug("Extracted TXT file: {}", filename);
                    }
                }
                zipIn.closeEntry();
            }

            logger.info("Extracted {} TXT files from ZIP to input directory", extractedCount);

            if (extractedCount == 0) {
                throw new NoContentException("No TXT files found in the uploaded ZIP file");
            }
        }
    }

    /**
     * RAR file processing - currently not supported
     * @param file The uploaded RAR file
     * @param inputDir The input directory
     */
    private void processRarFile(MultipartFile file, Path inputDir) throws IOException {
        logger.warn("RAR file processing not fully implemented yet: {}", file.getOriginalFilename());
        throw new FileProcessingException("RAR file processing is not yet supported. Please use ZIP files.");
    }

    /**
     * Prepares the input directory - creates if it doesn't exist, cleans up old TXT files if it does
     * @param inputDir The input directory
     */
    private void prepareInputDirectory(Path inputDir) {
        try {
            if (!Files.exists(inputDir)) {
                Files.createDirectories(inputDir);
                logger.info("Created input directory: {}", inputDir);
            } else {
                // Clean up old TXT files
                cleanupOldTxtFiles(inputDir);
            }
        } catch (IOException e) {
            throw new DirectoryAccessException("Failed to prepare input directory: " + inputDir, e);
        }
    }

    /**
     * Cleans up old TXT files in the input directory
     * @param inputDir The input directory
     */
    private void cleanupOldTxtFiles(Path inputDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.txt")) {
            for (Path file : stream) {
                Files.delete(file);
                logger.debug("Deleted old TXT file: {}", file.getFileName());
            }
        }
    }


    /**
     * Validates file type
     * @param filename The filename to check
     * @return True if supported file type
     */
    public boolean isValidFileType(String filename) {
        if (filename == null) return false;

        String lowerCaseFileName = filename.toLowerCase();
        return lowerCaseFileName.endsWith(".txt") ||
                lowerCaseFileName.endsWith(".zip");
    }

    /**
     * Sanitizes the filename to make it safe
     * @param originalFilename The original filename
     * @return Sanitized filename
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) return "unknown";

        // Remove dangerous characters
        String sanitized = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Truncate if filename is too long
        if (sanitized.length() > 100) {
            String extension = "";
            int lastDotIndex = sanitized.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = sanitized.substring(lastDotIndex);
                sanitized = sanitized.substring(0, Math.min(100 - extension.length(), lastDotIndex));
            } else {
                sanitized = sanitized.substring(0, 100);
            }
            sanitized += extension;
        }

        return sanitized;
    }
}