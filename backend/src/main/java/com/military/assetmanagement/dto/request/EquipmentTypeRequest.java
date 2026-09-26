package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentTypeRequest {
    @NotBlank
    private String name;
    @NotNull
    private String category; // WEAPON, VEHICLE, AMMUNITION
    private String unit;
}
