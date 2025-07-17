package com.infina.fileanalyzer.service;

import com.infina.fileanalyzer.entity.ArchiveInfo;
import com.infina.fileanalyzer.service.abstracts.IArchvieService;
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
    public void unzip(String zipFilePath, String destDirectory) throws IOException{
        File theDestDirectory = new File(destDirectory);
        if (!theDestDirectory.exists()){
            theDestDirectory.mkdirs();
        }
        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFilePath))){
            ZipEntry entry = zipInput.getNextEntry();
            while (entry != null){
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()){
                    extractFile(zipInput, filePath);
                } else {
                    File directory = new File(filePath);
                    directory.mkdirs();
                }
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }
        }
        System.out.println("Unzip process completed: "+ destDirectory);
    }
    private void extractFile(ZipInputStream zipInput, String filePath) throws IOException{
        int BUFFER_SIZE = 4096;
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath))){
                byte[] bytesIn = new byte[BUFFER_SIZE];
                int read;
                while ((read = zipInput.read(bytesIn)) != -1){
                    bufferedOutputStream.write(bytesIn, 0, read);
            }
        }
    }

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
