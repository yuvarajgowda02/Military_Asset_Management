package com.military.assetmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String fullName;
    @NotNull
    private String role; // ADMIN, BASE_COMMANDER, LOGISTICS_OFFICER
    private Long baseId; // required unless ADMIN
}
