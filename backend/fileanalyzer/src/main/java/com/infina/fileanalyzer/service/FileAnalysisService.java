package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import com.infina.fileanalyzer.exception.directory.DirectoryNotFoundException;
import com.infina.fileanalyzer.exception.directory.DirectoryAccessException;
import com.infina.fileanalyzer.exception.file.FileProcessingException;
import com.infina.fileanalyzer.exception.file.FileNotFoundException;
import com.infina.fileanalyzer.exception.archive.ArchiveExtractionException;
import com.infina.fileanalyzer.exception.status.NoContentException;
import com.infina.fileanalyzer.service.abstracts.IFileAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles file discovery, directory management,
 * timestamped archive naming, and delegates actual analysis/unzip work
 * to lower-level components.
 */
@Service
public class FileAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisService.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final IFileAnalysisService coreAnalysisService;
    private final ArchiveService archiveService;

    @Value("${file.analyzer.input.directory}")
    private String inputDirectory;

    @Value("${file.analyzer.output.directory}")
    private String outputDirectory;

    @Value("${file.analyzer.extract.directory}")
    private String extractDirectory;

    public FileAnalysisService(IFileAnalysisService coreAnalysisService,
                               ArchiveService archiveService) {
        this.coreAnalysisService = coreAnalysisService;
        this.archiveService = archiveService;
    }

    /**
     * Analyzes all text files in the input directory and creates a ZIP archive.
     * Validates directory existence, finds text files, processes them, and returns analysis results.
     *
     * @return FileAnalysisResponseDto containing analysis results and archive information
     * @throws DirectoryNotFoundException if input or output directory doesn't exist and can't be created
     * @throws NoContentException         if no text files are found for processing
     * @throws FileProcessingException    if file analysis or archive creation fails
     */
    public FileAnalysisResponseDto analyzeAllFiles() {
        Path inDir = Paths.get(inputDirectory);
        if (!Files.exists(inDir)) {
            throw new DirectoryNotFoundException(
                    "Input directory not found: " + inputDirectory);
        }

        Path outDir = Paths.get(outputDirectory);
        try {
            if (!Files.exists(outDir)) {
                Files.createDirectories(outDir);
                logger.info("Created output directory '{}'", outputDirectory);
            }
        } catch (IOException e) {
            throw new DirectoryAccessException(
                    "Unable to create output directory: " + outputDirectory, e);
        }

        List<Path> txtFiles;
        try {
            txtFiles = Files.list(inDir)
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new DirectoryAccessException(
                    "Failed to list files in input directory: " + inputDirectory, e);
        }

        if (txtFiles.isEmpty()) {
            throw new NoContentException(
                    "No .txt files found in directory: " + inputDirectory);
        }

        String timestamp = LocalDateTime.now().format(TS_FMT);
        String archiveName = "archive_" + timestamp + ".zip";
        String outputZipPath = outputDirectory + File.separator + archiveName;

        try {
            return coreAnalysisService.processFile(
                    txtFiles, inputDirectory, outputZipPath);
        } catch (FileProcessingException | DirectoryNotFoundException ex) {
            // Let your global exception handler map these
            throw ex;
        } catch (Exception ex) {
            throw new FileProcessingException(
                    "File analysis failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Extracts a ZIP file to a timestamped subdirectory.
     * Handles both absolute and relative ZIP paths, validates file existence,
     * creates the extraction directory if needed, and returns extraction details.
     *
     * @param zipFilePath Path to the ZIP file (absolute or relative to output directory)
     * @return Map containing extraction status and path information
     * @throws FileNotFoundException      if the ZIP file doesn't exist
     * @throws DirectoryAccessException   if extract directory creation fails
     * @throws ArchiveExtractionException if extraction fails
     */
    public Map<String, String> unzipFile(String zipFilePath) {
        Path zipPath = Paths.get(zipFilePath);
        if (!zipPath.isAbsolute()) {
            zipPath = Paths.get(outputDirectory, zipFilePath);
        }
        if (!Files.exists(zipPath)) {
            throw new FileNotFoundException(
                    "ZIP file not found: " + zipPath);
        }

        Path extractRoot = Paths.get(extractDirectory);
        try {
            if (!Files.exists(extractRoot)) {
                Files.createDirectories(extractRoot);
                logger.info("Created extract directory '{}'", extractDirectory);
            }
        } catch (IOException e) {
            throw new DirectoryAccessException(
                    "Unable to create extract directory: " + extractDirectory, e);
        }

        String subdir = "extract_" + LocalDateTime.now().format(TS_FMT);
        String targetDir = extractDirectory + File.separator + subdir;

        try {
            archiveService.unzip(zipPath.toString(), targetDir);
        } catch (ArchiveExtractionException ex) {
            // your handler will map this
            throw ex;
        } catch (Exception ex) {
            throw new ArchiveExtractionException(
                    "Failed to extract ZIP file: " + ex.getMessage(), ex);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("sourceZipFile", zipPath.toString());
        response.put("extractionDirectory", targetDir);
        return response;
    }

    /**
     * Returns the current directory configuration.
     *
     * @return Map containing input, output, and extract directory paths
     */
    public Map<String, String> getConfiguration() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("inputDirectory", inputDirectory);
        cfg.put("outputDirectory", outputDirectory);
        cfg.put("extractDirectory", extractDirectory);
        return cfg;
    }
}
