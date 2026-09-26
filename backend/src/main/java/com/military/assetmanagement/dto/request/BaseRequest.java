package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaseRequest {
    @NotBlank
    private String name;
    private String location;
}
