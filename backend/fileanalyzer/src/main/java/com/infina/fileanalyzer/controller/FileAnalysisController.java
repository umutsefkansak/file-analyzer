package com.infina.fileanalyzer.controller;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import com.infina.fileanalyzer.service.FileAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisController.class);
    private final FileAnalysisService fileAnalysisService;

    @Autowired
    public FileAnalysisController(FileAnalysisService fileAnalysisService) {
        this.fileAnalysisService = fileAnalysisService;
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
}