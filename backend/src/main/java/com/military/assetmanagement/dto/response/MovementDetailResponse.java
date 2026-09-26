package com.military.assetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementDetailResponse {
    private List<PurchaseView> purchases;
    private List<TransferView> transfersIn;
    private List<TransferView> transfersOut;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseView {
        private Long id;
        private String baseName;
        private String equipmentType;
        private Integer quantity;
        private String purchaseDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferView {
        private Long id;
        private String fromBase;
        private String toBase;
        private String equipmentType;
        private Integer quantity;
        private String transferDate;
    }
}
