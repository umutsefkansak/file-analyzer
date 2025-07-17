package com.infina.fileanalyzer.service.abstracts;

import com.infina.fileanalyzer.entity.ArchiveInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


/**
 * Interface for archive operations
 */
public interface IArchvieService {
    /**
     * Creates an archive (ZIP) from files in the specified directory
     * @param inputDirectory The directory containing files to archive
     * @param outputZipPath The path where the ZIP file will be created
     * @return ArchiveInfo containing information about the archiving process
     */
    ArchiveInfo createArchive(String inputDirectory, String outputZipPath);

    /**
     * Finds all .txt files in the specified directory
     * @param inputDirectory The directory to search for .txt files
     * @return List of Path objects representing found .txt files
     * @throws IOException if an I/O error occurs
     */
    List<Path> findTxtFiles(String inputDirectory) throws IOException;

    /**
     * Deletes the source files after archiving (optional operation)
     * @param files List of files to delete
     */
    void deleteSourceFiles(List<Path> files);
}
