package com.example.attendance_system.entity;

/**
 * Enumeration representing user roles in the attendance system.
 */
public enum Role {
    
    USER("ROLE_USER", "Standard User"),
    ADMIN("ROLE_ADMIN", "Administrator"),
    MANAGER("ROLE_MANAGER", "Manager");
    
    private final String authority;
    private final String displayName;
    
    Role(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }
    
    public String getAuthority() {
        return authority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
