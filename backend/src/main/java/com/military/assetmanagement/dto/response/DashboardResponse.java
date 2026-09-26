package com.military.assetmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long openingBalance;
    private long closingBalance;
    private long purchases;
    private long transferIn;
    private long transferOut;
    private long netMovement;
    private long assigned;
    private long expended;
}
