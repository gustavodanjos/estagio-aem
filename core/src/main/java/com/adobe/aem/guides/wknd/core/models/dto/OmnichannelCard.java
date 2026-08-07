package com.adobe.aem.guides.wknd.core.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface OmnichannelCard {
    
    @JsonProperty("title")
    String getTitle();

    @JsonProperty("path")
    String getPath();

    @JsonProperty("imagePath")
    String getImagePath();

    @JsonProperty("description")
    String getDescription();

    @JsonProperty("lastModified")
    String getLastModified();

    @JsonProperty("type")
    String getType();

    // Optional fields for Adventures
    @JsonProperty("price")
    default Double getPrice() {
        return null;
    }

    @JsonProperty("priceFormatted")
    default String getPriceFormatted() {
        return null;
    }

    @JsonProperty("difficulty")
    default String getDifficulty() {
        return null;
    }

    @JsonProperty("guideName")
    default String getGuideName() {
        return null;
    }
}
