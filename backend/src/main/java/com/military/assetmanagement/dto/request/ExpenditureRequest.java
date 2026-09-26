package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenditureRequest {
    @NotNull
    private Long baseId;
    @NotNull
    private Long equipmentTypeId;
    @NotNull
    @Positive
    private Integer quantity;
    @NotNull
    private LocalDate expendedDate;
    private String reason;
}
