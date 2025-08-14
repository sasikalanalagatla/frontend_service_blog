package com.mb.frontendService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private UserDto user;
    private String token;
    
    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
        this.success = true;
        this.message = "Success";
    }
    
    public AuthResponse(String message) {
        this.success = false;
        this.message = message;
    }
}
