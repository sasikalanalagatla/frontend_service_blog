package com.mb.frontendService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    
    private Long id;
    private String comment;
    private String name;
    private String email;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
