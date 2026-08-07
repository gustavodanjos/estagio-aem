package com.adobe.aem.guides.wknd.core.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MagazineArticle implements OmnichannelCard {

    @JsonProperty("title")
    private String title;

    @JsonProperty("path")
    private String path;

    @JsonProperty("imagePath")
    private String imagePath;

    @JsonProperty("description")
    private String description;

    @JsonProperty("lastModified")
    private String lastModified;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private long rawDate;

    private String type = "MATERIA";

    public MagazineArticle() {
    }

    public MagazineArticle(String title, String path, String imagePath, String description, String lastModified, long rawDate) {
        this.title = title;
        this.path = path;
        this.imagePath = imagePath;
        this.description = description;
        this.lastModified = lastModified;
        this.rawDate = rawDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public long getRawDate() {
        return rawDate;
    }
}
