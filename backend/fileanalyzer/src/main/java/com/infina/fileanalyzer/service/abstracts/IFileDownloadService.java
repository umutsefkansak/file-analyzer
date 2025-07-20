package com.infina.fileanalyzer.service.abstracts;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface IFileDownloadService {

    /**
     * Downloads a file by filename from the configured output directory
     * @param filename Name of the file to download
     * @return ResponseEntity containing the file resource or appropriate error response
     */
    ResponseEntity<Resource> downloadFile(String filename);
}