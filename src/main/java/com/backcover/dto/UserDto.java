package com.backcover.dto;

import java.util.UUID;

public class UserDto {
    private UUID id;
    private String email;
    private String supabaseUserId;

    public UserDto() {
    }

    public UserDto(UUID id, String email, String supabaseUserId) {
        this.id = id;
        this.email = email;
        this.supabaseUserId = supabaseUserId;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSupabaseUserId() {
        return supabaseUserId;
    }

    // Setters (optionnels, dépend de si vous voulez le rendre mutable)
    public void setId(UUID id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSupabaseUserId(String supabaseUserId) {
        this.supabaseUserId = supabaseUserId;
    }
}