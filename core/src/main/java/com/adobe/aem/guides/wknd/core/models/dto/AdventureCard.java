package com.adobe.aem.guides.wknd.core.models.dto;

public class AdventureCard implements OmnichannelCard {

    private String title;
    private String path;
    private String imagePath;
    private String description;
    private String lastModified;
    private String type = "AVENTURA";
    private Double price;
    private String difficulty;
    private String guideName;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private long rawDate;

    public AdventureCard(String title, String path, String imagePath, String description, String lastModified, Double price, String difficulty, String guideName, long rawDate) {
        this.title = title;
        this.path = path;
        this.imagePath = imagePath;
        this.description = description;
        this.lastModified = lastModified;
        this.price = price;
        this.difficulty = difficulty;
        this.guideName = guideName;
        this.rawDate = rawDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLastModified() {
        return lastModified;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Double getPrice() {
        return price;
    }

    @Override
    public String getPriceFormatted() {
        if (price == null) return "$0.00";
        return String.format(java.util.Locale.US, "$%.2f", price);
    }

    @Override
    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public String getGuideName() {
        return guideName;
    }

    @Override
    public long getRawDate() {
        return rawDate;
    }
}
