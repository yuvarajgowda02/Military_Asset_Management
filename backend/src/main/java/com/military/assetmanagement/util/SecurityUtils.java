package com.military.assetmanagement.util;

import com.military.assetmanagement.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(currentUser().getRole());
    }

    public static boolean isBaseCommander() {
        return "BASE_COMMANDER".equals(currentUser().getRole());
    }

    public static boolean isLogisticsOfficer() {
        return "LOGISTICS_OFFICER".equals(currentUser().getRole());
    }

    /**
     * Resolves the effective base id a non-admin user is restricted to.
     * Returns null for ADMIN (meaning: no restriction / all bases).
     */
    public static Long restrictedBaseId() {
        UserPrincipal user = currentUser();
        if ("ADMIN".equals(user.getRole())) {
            return null;
        }
        return user.getBaseId();
    }
}
