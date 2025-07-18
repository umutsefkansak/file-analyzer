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

@Service
public class ArchiveService implements IArchvieService {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

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