package com.educms.model;

import java.time.LocalDateTime;

/** Represents an uploaded media asset (image, video, document, etc). */
public class MediaFile {
    private int id;
    private String filename;
    private String filepath;
    private String fileType;
    private long fileSize;
    private String description;
    private Integer linkedContentId; // nullable
    private int uploadedBy;
    private String uploaderName; // joined field
    private LocalDateTime uploadedAt;

    public MediaFile() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFilepath() { return filepath; }
    public void setFilepath(String filepath) { this.filepath = filepath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getLinkedContentId() { return linkedContentId; }
    public void setLinkedContentId(Integer linkedContentId) { this.linkedContentId = linkedContentId; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    /** Human readable file size, e.g. "1.4 MB". */
    public String getReadableSize() {
        if (fileSize < 1024) return fileSize + " B";
        int exp = (int) (Math.log(fileSize) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", fileSize / Math.pow(1024, exp), unit);
    }
}
