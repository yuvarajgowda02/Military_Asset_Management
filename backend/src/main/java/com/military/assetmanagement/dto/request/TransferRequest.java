package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransferRequest {
    @NotNull
    private Long fromBaseId;
    @NotNull
    private Long toBaseId;
    @NotNull
    private Long equipmentTypeId;
    @NotNull
    @Positive
    private Integer quantity;
    @NotNull
    private LocalDate transferDate;
    private String remarks;
}
