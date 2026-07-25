package com.aiblog.dto;

import com.aiblog.model.DimensionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AestheticsDimensionRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotNull(message = "Type is required")
    private DimensionType type;

    private Integer displayOrder;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
