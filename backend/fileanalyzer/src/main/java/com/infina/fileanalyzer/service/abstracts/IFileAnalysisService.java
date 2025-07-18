package com.infina.fileanalyzer.service.abstracts;

import com.infina.fileanalyzer.dto.FileAnalysisResponseDto;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for file analysis service that orchestrates the file analysis process.
 * Coordinates file processing, result calculation, and archiving operations.
 */
public interface IFileAnalysisService {

    /**
     * Processes a list of files by analyzing their content, calculating total results,
     * and creating an archive of the processed files.
     *
     * @param filePaths List of paths to the files to be processed
     * @param inputDirectory Directory containing the input files
     * @param outputZipPath Path where the output ZIP file will be created
     * @return FileAnalysisResponseDto containing both analysis results and archive information
     */
    FileAnalysisResponseDto processFile(List<Path> filePaths, String inputDirectory, String outputZipPath);
}
