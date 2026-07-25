package com.aiblog.dto;

import com.aiblog.model.AestheticsDimension;
import com.aiblog.model.DimensionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AestheticsDimensionResponse {

    private Long id;
    private String name;
    private DimensionType type;
    private int displayOrder;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AestheticsDimensionResponse fromEntity(AestheticsDimension dimension) {
        AestheticsDimensionResponse response = new AestheticsDimensionResponse();
        response.setId(dimension.getId());
        response.setName(dimension.getName());
        response.setType(dimension.getType());
        response.setDisplayOrder(dimension.getDisplayOrder());
        response.setDescription(dimension.getDescription());
        response.setCreatedAt(dimension.getCreatedAt());
        response.setUpdatedAt(dimension.getUpdatedAt());
        return response;
    }
}
