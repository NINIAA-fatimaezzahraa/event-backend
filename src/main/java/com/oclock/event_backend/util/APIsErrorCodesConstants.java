package com.oclock.event_backend.util;

public final class APIsErrorCodesConstants {

    // ################ Error messages for JWT and token handling ################
    public static final String UNAUTHORIZED = "Unauthorized: Please log in first.";
    public static final String ACCESS_DENIED = "Access Denied: You are not authorized to access this resource.";
    public static final String JWT_EXPIRED = "JWT token is expired: %s";
    public static final String INVALID_TOKEN = "Invalid JWT token: %s";
    public static final String USER_DISABLED = "User is inactive.";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred";

    // #################  Refresh token related error messages ##################
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required.";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token.";
    public static final String EXPIRED_REFRESH_TOKEN = "Expired refresh token.";

    private APIsErrorCodesConstants() {
        // Private constructor to prevent instantiation
    }
}
