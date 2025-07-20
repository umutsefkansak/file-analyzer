import React, { useState } from 'react';
import { Upload, FileText, Clock, Archive, CheckCircle, AlertCircle, Loader } from 'lucide-react';
import '../styles/FileAnalyzer.css';

const FileAnalyzerApp = () => {
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [analysisResult, setAnalysisResult] = useState(null);
    const [error, setError] = useState(null);
    const [uploadMode, setUploadMode] = useState('single'); // 'single' or 'multiple'
    const [downloadUrl, setDownloadUrl] = useState(null);


  

    const handleFileSelect = (event) => {
    const files = Array.from(event.target.files);
    
    if (uploadMode === 'multiple') {
        setSelectedFiles(prevFiles => [...prevFiles, ...files]);
    } else {
        setSelectedFiles(files);
    }
    
    setError(null);
    setAnalysisResult(null);
    
    event.target.value = '';
    };

    const handleDragOver = (event) => {
        event.preventDefault();
    };

    const handleDrop = (event) => {
        event.preventDefault();
        const files = Array.from(event.dataTransfer.files);
        setSelectedFiles(files);
        setError(null);
        setAnalysisResult(null);
    };

    const formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };


    const formatDateTime = (dateTimeStr) => {
        if (!dateTimeStr) return '-';
        return dateTimeStr.replace('T', ' ');
    };

    const uploadAndAnalyze = async () => {
        if (selectedFiles.length === 0) {
            setError('Lütfen en az bir dosya seçin');
            return;
        }

        setIsLoading(true);
        setError(null);

        const formData = new FormData();

        if (uploadMode === 'single' && selectedFiles.length > 0) {
            formData.append('file', selectedFiles[0]);
        } else {
            selectedFiles.forEach((file) => {
                formData.append('files', file);
            });
        }

        try {
            const endpoint = uploadMode === 'single'
                ? '/api/v1/files/upload-and-analyze'
                : '/api/v1/files/upload-multiple-and-analyze';

            const response = await fetch(`http://localhost:8080${endpoint}`, {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            setAnalysisResult(result);
        } catch (err) {
            setError('Dosya analizi sırasında bir hata oluştu: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const clearFiles = () => {
        setSelectedFiles([]);
        setAnalysisResult(null);
        setError(null);
    };

    const downloadZipFile = async () => {
        if (!analysisResult?.archiveInfo?.archiveFileName) {
            setError('İndirilecek dosya bulunamadı');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/v1/files/download/${analysisResult.archiveInfo.archiveFileName}`, {
                method: 'GET',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = analysisResult.archiveInfo.archiveFileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            setError('Dosya indirme sırasında bir hata oluştu: ' + err.message);
        }
    };

    return (
        <div className="app-container">
            <div className="content-wrapper">
                <div className="header">
                    <h1 className="main-title">
                        Dosya Analiz Sistemi
                    </h1>
                    <p className="subtitle">
                        TXT, ZIP ve RAR dosyalarınızı yükleyin ve detaylı analiz sonuçlarını görüntüleyin
                    </p>
                </div>

                {/* Upload Mode Selection */}
                <div className="upload-section">
                    <div className="mode-selection">
                        <button
                            onClick={() => setUploadMode('single')}
                            className={`mode-button ${uploadMode === 'single' ? 'active' : ''}`}
                        >
                            Tek Dosya
                        </button>
                        <button
                            onClick={() => setUploadMode('multiple')}
                            className={`mode-button ${uploadMode === 'multiple' ? 'active' : ''}`}
                        >
                            Çoklu Dosya
                        </button>
                    </div>

                    {/* File Upload Area */}
                    <div
                        onDragOver={handleDragOver}
                        onDrop={handleDrop}
                        className="upload-area"
                    >
                        <input
                            type="file"
                            onChange={handleFileSelect}
                            multiple={uploadMode === 'multiple'}
                            accept=".txt,.zip,.rar"
                            id="fileInput"
                        />
                        <label htmlFor="fileInput" style={{ cursor: 'pointer' }}>
                            <Upload className="upload-icon" />
                            <h3 className="upload-title">
                                Dosyalarınızı buraya sürükleyin veya tıklayın
                            </h3>
                            <p className="upload-description">
                                TXT, ZIP, RAR dosyaları desteklenmektedir
                            </p>
                        </label>
                    </div>

                    {/* Selected Files */}
                    {selectedFiles.length > 0 && (
                        <div className="selected-files">
                            <h4 className="selected-files-title">Seçilen Dosyalar:</h4>
                            <div className="file-list">
                                {selectedFiles.map((file, index) => (
                                    <div key={index} className="file-item">
                                        <div className="file-info">
                                            <FileText className="file-icon" />
                                            <span className="file-name">{file.name}</span>
                                            <span className="file-size">
                                                ({formatFileSize(file.size)})
                                            </span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            <div className="action-buttons">
                                <button
                                    onClick={uploadAndAnalyze}
                                    disabled={isLoading}
                                    className="btn btn-primary"
                                >
                                    {isLoading ? (
                                        <Loader className="btn-icon spin" />
                                    ) : (
                                        <Upload className="btn-icon" />
                                    )}
                                    <span>{isLoading ? 'Analiz Ediliyor...' : 'Analiz Et'}</span>
                                </button>
                                <button
                                    onClick={clearFiles}
                                    className="btn btn-secondary"
                                    style={{
                                        backgroundColor: '#dc3545',
                                        borderColor: '#dc3545',
                                        color: 'white'
                                    }}
                                >
                                    Temizle
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                {/* Error Message */}
                {error && (
                    <div className="error-message">
                        <AlertCircle className="error-icon" />
                        <p className="error-text">{error}</p>
                    </div>
                )}

                {/* Analysis Results */}
                {analysisResult && (
                    <div className="results-section">
                        {/* Summary Cards */}
                        <div className="summary-grid">
                            <div className="summary-card">
                                <div className="card-content">
                                    <div>
                                        <p className="card-text">Toplam Dosya</p>
                                        <p className="card-value blue">
                                            {analysisResult.totalResult.totalProcessedFiles}
                                        </p>
                                    </div>
                                    <FileText className="card-icon blue" />
                                </div>
                            </div>

                            <div className="summary-card">
                                <div className="card-content">
                                    <div>
                                        <p className="card-text">Toplam Satır</p>
                                        <p className="card-value green">
                                            {analysisResult.totalResult.totalLineCount}
                                        </p>
                                    </div>
                                    <CheckCircle className="card-icon green" />
                                </div>
                            </div>

                            <div className="summary-card">
                                <div className="card-content">
                                    <div>
                                        <p className="card-text">Toplam Karakter</p>
                                        <p className="card-value purple">
                                            {analysisResult.totalResult.totalCharacterCount}
                                        </p>
                                    </div>
                                    <FileText className="card-icon purple" />
                                </div>
                            </div>

                            <div className="summary-card">
                                <div className="card-content">
                                    <div>
                                        <p className="card-text">İşlem Süresi</p>
                                        <p className="card-value orange">
                                            {analysisResult.totalResult.totalProcessingTimeMillis?.toFixed(2)} ms
                                        </p>
                                    </div>
                                    <Clock className="card-icon orange" />
                                </div>
                            </div>
                        </div>

                        {/* File Details Table */}
                        <div className="table-container">
                            <div className="table-header">
                                <h3 className="table-title">Dosya Detayları</h3>
                            </div>
                            <div className="table-wrapper">
                                <table className="data-table">
                                    <thead className="table-head">
                                    <tr>
                                        <th>Dosya Adı</th>
                                        <th>Satır Sayısı</th>
                                        <th>Karakter Sayısı</th>
                                        <th>İşlem Süresi</th>
                                        <th>Başlangıç Zamanı</th>
                                        <th>Bitiş Zamanı</th>
                                        <th>Thread</th>
                                        <th>Durum</th>
                                    </tr>
                                    </thead>
                                    <tbody className="table-body">
                                    {analysisResult.totalResult.fileStatsList?.map((file, index) => (
                                        <tr key={index}>
                                            <td className="cell-primary">
                                                {file.fileName}
                                            </td>
                                            <td className="cell-secondary">
                                                {file.lineCount}
                                            </td>
                                            <td className="cell-secondary">
                                                {file.characterCount}
                                            </td>
                                            <td className="cell-secondary">
                                                {file.processingTimeMillis?.toFixed(2)} ms
                                            </td>
                                            <td className="cell-secondary">
                                                {formatDateTime(file.processingStartTime)}
                                            </td>
                                            <td className="cell-secondary">
                                                {formatDateTime(file.processingEndTime)}
                                            </td>
                                            <td className="cell-secondary">
                                                {file.threadName}
                                            </td>
                                            <td>
                                                {file.processingCompleted ? (
                                                    <span className="status-badge success">
                                                            <CheckCircle className="status-icon"/>
                                                            Tamamlandı
                                                        </span>
                                                ) : (
                                                    <span className="status-badge error">
                                                            <AlertCircle className="status-icon"/>
                                                            Başarısız
                                                        </span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Archive Information */}
                        {analysisResult.archiveInfo && (
                            <div className="archive-info">
                                <div className="archive-header">
                                    <Archive className="archive-icon"/>
                                    <h3 className="archive-title">Arşiv Bilgileri</h3>
                                </div>
                                <div className="archive-grid">
                                    <div className="info-item">
                                        <p className="info-label">Arşiv Dosyası</p>
                                        <p className="info-value">{analysisResult.archiveInfo.archiveFileName}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Dosya Boyutu</p>
                                        <p className="info-value">{formatFileSize(analysisResult.archiveInfo.archiveFileSizeBytes)}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Arşivlenen Dosya Sayısı</p>
                                        <p className="info-value">{analysisResult.archiveInfo.archivedFileCount}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Sıkıştırma Yöntemi</p>
                                        <p className="info-value">{analysisResult.archiveInfo.compressionMethod}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Arşivleme Süresi</p>
                                        <p className="info-value">{analysisResult.archiveInfo.archiveProcessingTimeMillis?.toFixed(2)} ms</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Arşiv Başlangıç</p>
                                        <p className="info-value">{formatDateTime(analysisResult.archiveInfo.archiveStartTime)}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Arşiv Bitiş</p>
                                        <p className="info-value">{formatDateTime(analysisResult.archiveInfo.archiveEndTime)}</p>
                                    </div>
                                    <div className="info-item">
                                        <p className="info-label">Thread</p>
                                        <p className="info-value">{analysisResult.archiveInfo.threadName}</p>
                                    </div>
                                </div>
                                <div className="download-section" style={{marginTop: '20px', textAlign: 'center'}}>
                                    <button
                                        onClick={downloadZipFile}
                                        className="btn btn-primary"
                                        style={{
                                            backgroundColor: '#10b981',
                                            borderColor: '#10b981',
                                            padding: '12px 24px',
                                            fontSize: '16px',
                                            fontWeight: 'bold'
                                        }}
                                    >
                                        <Archive className="btn-icon"/>
                                        <span>ZIP Dosyasını İndir</span>
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default FileAnalyzerApp;