package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.service.abstracts.IFileDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileDownloadService implements IFileDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadService.class);

    @Value("${file.analyzer.output.directory}")
    private String outputDirectory;

    /**
     * Downloads a file by filename from the configured output directory
     * @param filename Name of the file to download
     * @return ResponseEntity containing the file resource or appropriate error response
     */
    @Override
    public ResponseEntity<Resource> downloadFile(String filename) {
        try {
            logger.info("Download request received for file: {}", filename);

            Path filePath = Paths.get(outputDirectory).resolve(filename);

            // Check if file exists
            if (!Files.exists(filePath)) {
                logger.warn("File not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Security check - ensure file is within output directory
            Path normalizedFilePath = filePath.normalize();
            Path normalizedOutputPath = Paths.get(outputDirectory).normalize();
            if (!normalizedFilePath.startsWith(normalizedOutputPath)) {
                logger.warn("Security violation: Attempted to access file outside output directory: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(normalizedFilePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.warn("File exists but is not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }

            logger.info("File download successful: {}", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, determineContentType(filename))
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error downloading file: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determines the content type based on file extension
     * @param filename Name of the file
     * @return Content type string
     */
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        String lowerCaseFilename = filename.toLowerCase();
        if (lowerCaseFilename.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerCaseFilename.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerCaseFilename.endsWith(".rar")) {
            return "application/x-rar-compressed";
        }

        return "application/octet-stream";
    }
}