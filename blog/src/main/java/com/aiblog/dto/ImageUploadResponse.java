package com.aiblog.dto;

/**
 * Response payload for a successful image upload.
 * Contains the public URL path at which the stored image can be accessed.
 */
public class ImageUploadResponse {

    private final String url;

    public ImageUploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
