package com.example.elhabashyback.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConvertor implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return role.getRole();
    }

    @Override
    public Role convertToEntityAttribute(String role) {
        if(role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return Role.fromString(role);
    }
}
