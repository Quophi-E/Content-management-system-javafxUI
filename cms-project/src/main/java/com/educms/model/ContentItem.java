package com.educms.model;

import java.time.LocalDateTime;

/** Unified model for Articles and Blog posts -- they share the same shape,
 *  differentiated by {@link ContentType}. */
public class ContentItem {
    private int id;
    private ContentType type;
    private String title;
    private String body;
    private String category;
    private String tags;
    private ContentStatus status;
    private int authorId;
    private String authorName; // populated by joined queries, not persisted directly
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContentItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ContentType getType() { return type; }
    public void setType(ContentType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
