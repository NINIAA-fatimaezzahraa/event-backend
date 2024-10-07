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

    // ################# Event related error messages ##################
    public static final String EVENT_NOT_FOUND = "Event not found with id: %s";
    public static final String USER_NOT_FOUND = "User not found with email: %s";
    public static final String CATEGORY_NOT_FOUND = "The category '%s' does not exist. Please provide a valid event category.";
    public static final String SPONSOR_NOT_FOUND = "Sponsor with id %s does not exist.";
    public static final String SPONSOR_NOT_ASSOCIATED = "Sponsor with id %s is not associated with this event.";
    public static final String NOT_AUTHORIZED_TO_DELETE_EVENT = "You are not authorized to delete this event.";
    public static final String MANDATORY_DATE_PROVIDE = "Both startDate and endDate must be provided.";
    public static final String START_DATE_AFTER_END_DATE = "startDate must be before endDate.";
    public static final String DATABASE_CONSTRAINT_VIOLATION = "Failed to save event due to database constraints";

    private APIsErrorCodesConstants() {
        // Private constructor to prevent instantiation
    }
}
