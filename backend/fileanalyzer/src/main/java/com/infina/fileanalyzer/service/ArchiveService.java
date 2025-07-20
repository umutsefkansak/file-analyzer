package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.ArchiveInfo;
import com.infina.fileanalyzer.exception.archive.ArchiveCreationException;
import com.infina.fileanalyzer.exception.archive.ArchiveExtractionException;
import com.infina.fileanalyzer.exception.archive.InvalidArchiveException;
import com.infina.fileanalyzer.exception.directory.DirectoryAccessException;
import com.infina.fileanalyzer.exception.directory.DirectoryNotFoundException;
import com.infina.fileanalyzer.exception.file.FileNotFoundException;
import com.infina.fileanalyzer.service.abstracts.IArchvieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ArchiveService class manages file archiving and extraction operations.
 * This class is designed to be thread-safe and can work safely in multi-threaded environments.
 * Main functionalities:
 * - Compress .txt files from specified directory into ZIP archive
 * - Extract ZIP archives to specified directory
 * - Validate archive file integrity
 * - Delete source files

 */
@Service
public class ArchiveService implements IArchvieService {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

    /**
     * Compresses all .txt files from the specified directory into a ZIP archive.
     * This method performs the following operations:
     * 1. Validates the existence of the input directory
     * 2. Finds all .txt files in the directory
     * 3. Creates output directory if necessary
     * 4. Compresses files in ZIP format
     * 5. Records and returns archive information
     *
     * @param inputDirectory Directory containing .txt files to be archived
     * @param outputZipPath Full path of the ZIP file to be created
     * @return ArchiveInfo Object containing detailed information about the archiving process
     * @throws DirectoryNotFoundException If input directory is not found
     * @throws DirectoryAccessException If directory access error occurs
     * @throws ArchiveCreationException If ZIP creation error occurs
     */
    public ArchiveInfo createArchive(String inputDirectory, String outputZipPath){
        ArchiveInfo archiveInfo = new ArchiveInfo();
        List<String> archivedFileNames = new ArrayList<>();
        archiveInfo.setArchiveFileName(Paths.get(outputZipPath).getFileName().toString());
        archiveInfo.setArchiveFilePath(outputZipPath);
        LocalDateTime start = LocalDateTime.now();
        archiveInfo.setArchiveStartTime(start);

        try {
            // Check if input directory exists
            Path inputDirPath = Paths.get(inputDirectory);
            if (!Files.exists(inputDirPath)) {
                throw new DirectoryNotFoundException("Input directory not found: " + inputDirectory);
            }

            List<Path> txtFiles = findTxtFiles(inputDirectory);
            archiveInfo.setArchivedFileNames(new ArrayList<>());
            if (txtFiles.isEmpty()){
                logger.warn("No .txt files found to archive in directory: {}", inputDirectory);
                return archiveInfo;
            }

            Path outputDirectory = Paths.get(outputZipPath).getParent();
            if (outputDirectory != null && !Files.exists(outputDirectory)){
                try {
                    Files.createDirectories(outputDirectory);
                } catch (IOException e) {
                    throw new DirectoryAccessException("Failed to create output directory: " + outputDirectory, e);
                }
            }

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputZipPath))){
                for (Path file : txtFiles){
                    ZipEntry entry = new ZipEntry(file.getFileName().toString());
                    zipOutputStream.putNextEntry(entry);
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    archivedFileNames.add(file.getFileName().toString());
                }
            } catch (IOException e) {
                throw new ArchiveCreationException("Failed to create ZIP archive: " + e.getMessage(), e);
            }

            File zipFile = new File(outputZipPath);
            if (!zipFile.exists()) {
                throw new ArchiveCreationException("ZIP file was not created successfully: " + outputZipPath);
            }

            archiveInfo.setArchiveFileSizeBytes(zipFile.length());
            archiveInfo.setArchivedFileNames(archivedFileNames);
            archiveInfo.setArchivedFileCount(archivedFileNames.size());
            archiveInfo.setCompressionMethod("ZIP");
            archiveInfo.setThreadName(Thread.currentThread().getName());

            logger.info("Files zipped successfully to: {}", outputZipPath);
        } catch (IOException e){
            throw new ArchiveCreationException("An error occurred while archiving: " + e.getMessage(), e);
        }

        LocalDateTime end = LocalDateTime.now();
        archiveInfo.setArchiveEndTime(end);
        return archiveInfo;
    }

    /**
     * Finds all .txt files in the specified directory and returns them as a list.
     * This method scans directory contents and returns only files with .txt extension.
     * Uses DirectoryStream for directory access and automatically closes resources.
     *
     * @param inputDirectory Path of the directory to scan
     * @return List<Path> List of Path objects for found .txt files
     * @throws DirectoryNotFoundException If directory is not found
     * @throws DirectoryAccessException If directory access error occurs
     * @throws IOException If general I/O error occurs
     */
    public List<Path> findTxtFiles(String inputDirectory) throws IOException {
        List<Path> txtFiles = new ArrayList<>();
        Path dirPath = Paths.get(inputDirectory);

        if (!Files.exists(dirPath)) {
            throw new DirectoryNotFoundException("Directory not found: " + inputDirectory);
        }

        if (!Files.isDirectory(dirPath)) {
            throw new DirectoryAccessException("Path is not a directory: " + inputDirectory);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.txt")) {
            for (Path entry : stream) {
                txtFiles.add(entry);
            }
        } catch (IOException e) {
            throw new DirectoryAccessException("Failed to access directory: " + inputDirectory, e);
        }

        return txtFiles;
    }

    /**
     * Extracts ZIP archive to the specified directory.
     * This method performs the following operations:
     * 1. Validates the existence of the ZIP file
     * 2. Validates the integrity of the ZIP file
     * 3. Creates destination directory if necessary
     * 4. Extracts ZIP contents to file system
     *
     * @param zipFilePath Path of the ZIP file to extract
     * @param destDirectory Destination directory where files will be extracted
     * @throws FileNotFoundException If ZIP file is not found
     * @throws InvalidArchiveException If ZIP file is invalid or corrupted
     * @throws DirectoryAccessException If directory creation error occurs
     * @throws ArchiveExtractionException If extraction process error occurs
     */
    public void unzip(String zipFilePath, String destDirectory) {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            throw new FileNotFoundException("ZIP file not found: " + zipFilePath);
        }

        // Validate that the file is a valid ZIP archive
        if (!isValidZipFile(zipFile)) {
            throw new InvalidArchiveException("Invalid or corrupted ZIP file: " + zipFilePath);
        }

        File theDestDirectory = new File(destDirectory);
        if (!theDestDirectory.exists()) {
            if (!theDestDirectory.mkdirs()) {
                throw new DirectoryAccessException("Failed to create destination directory: " + destDirectory);
            }
        }

        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInput.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipInput, filePath);
                } else {
                    File directory = new File(filePath);
                    if (!directory.mkdirs() && !directory.exists()) {
                        throw new DirectoryAccessException("Failed to create directory during extraction: " + filePath);
                    }
                }
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }
            logger.info("Unzip process completed: {}", destDirectory);
        } catch (IOException e) {
            throw new ArchiveExtractionException("Failed to extract ZIP file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the integrity of a ZIP file.
     * This method verifies that the file is actually a valid ZIP archive by:
     * 1. Checking file size (must be at least 4 bytes)
     * 2. Checking ZIP file signature (0x504B0304)
     * 3. Testing ZIP structure with ZipInputStream
     *
     * @param file File to be validated
     * @return boolean True if file is a valid ZIP archive, false otherwise
     */
    private boolean isValidZipFile(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Check ZIP file signature (first 4 bytes should be 0x504B0304)
            if (raf.length() < 4) {
                return false;
            }

            int signature = raf.readInt();
            // ZIP files start with the signature 0x504B0304 (PK\3\4)
            if (signature != 0x504B0304) {
                return false;
            }

            // Try to open it as a ZipInputStream to check structure
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                // Try to read at least one entry
                return zis.getNextEntry() != null;
            }
        } catch (IOException e) {
            logger.warn("Failed to validate ZIP file: {}", file.getPath(), e);
            return false;
        }
    }

    /**
     * Extracts a single file from ZIP archive.
     * This method reads data from ZipInputStream and writes it to the specified location.
     * Uses BufferedOutputStream for performance improvement with 4KB buffer size.
     *
     * @param zipInput ZipInputStream used to read the file
     * @param filePath Full path where the file will be extracted
     * @throws IOException If file writing error occurs
     * @throws ArchiveExtractionException If extraction process error occurs
     */
    private void extractFile(ZipInputStream zipInput, String filePath) throws IOException {
        int BUFFER_SIZE = 4096;
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipInput.read(bytesIn)) != -1) {
                bufferedOutputStream.write(bytesIn, 0, read);
            }
        } catch (IOException e) {
            throw new ArchiveExtractionException("Failed to extract file: " + filePath, e);
        }
    }

    /**
     * Safely deletes specified files from the file system.
     * This method performs safe file deletion:
     * - Uses separate try-catch block for each file
     * - Does not stop process on deletion error, continues with other files
     * - Logs error conditions
     *
     * @param files List of Path objects for files to be deleted
     */
    public void deleteSourceFiles(List<Path> files) {
        for (Path file : files) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                logger.warn("Failed to delete source file: {}", file, e);
            }
        }
    }
}