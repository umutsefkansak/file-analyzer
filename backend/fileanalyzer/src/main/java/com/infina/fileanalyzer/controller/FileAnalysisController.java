package com.infina.fileanalyzer.controller;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import com.infina.fileanalyzer.service.FileAnalysisService;
import com.infina.fileanalyzer.service.abstracts.IFileDownloadService;
import com.infina.fileanalyzer.service.abstracts.IFileUploadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisController.class);
    private final FileAnalysisService fileAnalysisService;
    private final IFileUploadService fileUploadService;
    private final IFileDownloadService fileDownloadService;

    @Autowired
    public FileAnalysisController(FileAnalysisService fileAnalysisService, IFileUploadService fileUploadService, IFileDownloadService fileDownloadService) {
        this.fileAnalysisService = fileAnalysisService;
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<FileAnalysisResponseDto> analyzeFiles() {
        logger.info("Request received: analyze all .txt files");
        FileAnalysisResponseDto dto = fileAnalysisService.analyzeAllFiles();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/unzip")
    public ResponseEntity<Map<String, String>> unzipFile(@RequestParam String zipFilePath) {
        logger.info("Request received: unzip '{}'", zipFilePath);
        Map<String, String> result = fileAnalysisService.unzipFile(zipFilePath);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfiguration() {
        logger.info("Request received: get directory configuration");
        Map<String, String> config = fileAnalysisService.getConfiguration();
        return ResponseEntity.ok(config);
    }
    /**
     * File upload and analysis endpoint
     * @param file Uploaded file (ZIP, RAR or TXT)
     * @return Analysis results
     */
    @PostMapping("/upload-and-analyze")
    public ResponseEntity<FileAnalysisResponseDto> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file) {

        logger.info("File upload request received: {}", file.getOriginalFilename());

        FileAnalysisResponseDto result = fileUploadService.processUploadedFile(file);
        logger.info("File upload and analysis completed successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Multiple files upload and analysis endpoint
     * @param files Uploaded files (TXT, ZIP)
     * @return Analysis results
     */
    @PostMapping("/upload-multiple-and-analyze")
    public ResponseEntity<FileAnalysisResponseDto> uploadMultipleAndAnalyze(
            @RequestParam("files") MultipartFile[] files) {

        logger.info("Multiple file upload request received: {} files", files.length);

        FileAnalysisResponseDto result = fileUploadService.processMultipleUploadedFiles(files);
        logger.info("Multiple file upload and analysis completed successfully");
        return ResponseEntity.ok(result);
    }


    /**
     * Download file endpoint
     * @param filename Name of the file to download
     * @return File as ResponseEntity
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        logger.info("Download request received for: {}", filename);
        return fileDownloadService.downloadFile(filename);
    }
}