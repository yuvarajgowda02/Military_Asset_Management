package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseRequest {
    @NotNull
    private Long baseId;
    @NotNull
    private Long equipmentTypeId;
    @NotNull
    @Positive
    private Integer quantity;
    private BigDecimal unitCost;
    @NotNull
    private LocalDate purchaseDate;
    private String remarks;
}
