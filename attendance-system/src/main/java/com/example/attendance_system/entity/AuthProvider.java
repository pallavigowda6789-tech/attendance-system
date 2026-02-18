package com.example.attendance_system.entity;

/**
 * Enumeration representing authentication providers.
 */
public enum AuthProvider {
    
    LOCAL("Local", "Username/Password authentication"),
    GOOGLE("Google", "Google OAuth2 authentication"),
    GITHUB("GitHub", "GitHub OAuth2 authentication");
    
    private final String displayName;
    private final String description;
    
    AuthProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
