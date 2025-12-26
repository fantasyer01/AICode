package com.ithistory.dto;

import lombok.Data;

@Data
public class ImageDto {
    private String imageUrl;
    private String caption;
    private String altText;
    private Integer orderIndex;
}
