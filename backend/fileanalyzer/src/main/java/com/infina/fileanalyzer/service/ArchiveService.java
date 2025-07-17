package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.ArchiveInfo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ArchiveService {

    public ArchiveInfo createArchive(String inputDirectory, String outputZipPath){
        ArchiveInfo archiveInfo = new ArchiveInfo();
        List<String> archivedFileNames = new ArrayList<>();
        archiveInfo.setArchiveFileName(Paths.get(outputZipPath).getFileName().toString());
        archiveInfo.setArchiveFilePath(outputZipPath);
        LocalDateTime start = LocalDateTime.now();
        archiveInfo.setArchiveStartTime(start);

        try {
            List<Path> txtFiles = findTxtFiles(inputDirectory);
            archiveInfo.setArchivedFileNames(new ArrayList<>());
            if (txtFiles.isEmpty()){
                System.out.println(".txt file to archive not found ");
                return archiveInfo;
            }
            Path outputDirectory = Paths.get(outputZipPath).getParent();
            if (outputDirectory != null && !Files.exists(outputDirectory)){
                Files.createDirectories(outputDirectory);
            }
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputZipPath))){
                for (Path file : txtFiles){
                    ZipEntry entry = new ZipEntry(file.getFileName().toString());
                    zipOutputStream.putNextEntry(entry);
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    archivedFileNames.add(file.getFileName().toString());
                }
            }
            File zipFile = new File(outputZipPath);
            archiveInfo.setArchiveFileSizeBytes(zipFile.length());
            archiveInfo.setArchivedFileNames(archivedFileNames);
            archiveInfo.setArchivedFileCount(archivedFileNames.size());
            archiveInfo.setCompressionMethod("ZIP");
            archiveInfo.setThreadName(Thread.currentThread().getName());

            System.out.println("Files zipped successfully");
        } catch (IOException e){
            System.out.println("An error occurred while archiving: " + e.getMessage());
        }

        LocalDateTime end = LocalDateTime.now();
        archiveInfo.setArchiveEndTime(end);
        return archiveInfo;
    }

    public List<Path> findTxtFiles(String inputDirectory) throws IOException{
        List<Path> txtFiles = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inputDirectory), "*.txt")){
            for (Path entry : stream){
                txtFiles.add(entry);
            }
        }
        return txtFiles;
    }

    //optional
    public void deleteSourceFiles(List<Path> files){
        for (Path file : files){
            try{
                Files.deleteIfExists(file);
            } catch (IOException e){
                System.out.println(file + " could not be deleted: "+ e.getMessage());
            }
        }
    }



}
