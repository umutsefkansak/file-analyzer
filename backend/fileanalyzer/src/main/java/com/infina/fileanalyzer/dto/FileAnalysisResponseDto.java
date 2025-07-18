package com.infina.fileanalyzer.dto;

import com.infina.fileanalyzer.entity.AnalysisResult;
import com.infina.fileanalyzer.entity.ArchiveInfo;

/**
 * Data Transfer Object (DTO) that combines file analysis results and archive information
 * for returning to the client.
 */
public class FileAnalysisResponseDto {
    private AnalysisResult totalResult;
    private ArchiveInfo archiveInfo;

    public FileAnalysisResponseDto() {
    }

    /**
     * Constructor that builds the DTO from separate AnalysisResult and ArchiveInfo objects
     *
     * @param totalResult The analysis result
     * @param archiveInfo The archive information
     */
    public FileAnalysisResponseDto(AnalysisResult totalResult, ArchiveInfo archiveInfo) {
        this.totalResult = totalResult;
        this.archiveInfo = archiveInfo;
    }

    public AnalysisResult getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(AnalysisResult totalResult) {
        this.totalResult = totalResult;
    }

    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }

    public void setArchiveInfo(ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
    }
}