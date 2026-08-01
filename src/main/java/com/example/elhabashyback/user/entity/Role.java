package com.example.elhabashyback.user.entity;

public enum Role {
    USER("user"),
    ADMIN("admin");

    private final String role;

    Role(String role){
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static Role fromString(String role){
        if(role.equalsIgnoreCase("user")){
            return USER;
        }else if(role.equalsIgnoreCase("admin")){
            return ADMIN;
        }
        throw new IllegalArgumentException("Invalid Role");
    }
}
