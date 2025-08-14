package com.mb.frontendService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private String name;            
    private String email;
    private String password;       
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
