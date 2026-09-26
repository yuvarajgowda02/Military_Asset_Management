package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentRequest {
    @NotNull
    private Long baseId;
    @NotNull
    private Long equipmentTypeId;
    @NotBlank
    private String personnelName;
    private String personnelServiceNumber;
    @NotNull
    @Positive
    private Integer quantity;
    @NotNull
    private LocalDate assignedDate;
    private String remarks;
}
