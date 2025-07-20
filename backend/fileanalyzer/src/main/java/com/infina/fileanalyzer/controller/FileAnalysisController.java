package com.infina.fileanalyzer.controller;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import com.infina.fileanalyzer.service.FileAnalysisService;
import com.infina.fileanalyzer.service.abstracts.IFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisController.class);
    private final FileAnalysisService fileAnalysisService;
    private final IFileUploadService fileUploadService;

    @Autowired
    public FileAnalysisController(FileAnalysisService fileAnalysisService, IFileUploadService fileUploadService) {
        this.fileAnalysisService = fileAnalysisService;
        this.fileUploadService = fileUploadService;
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
}