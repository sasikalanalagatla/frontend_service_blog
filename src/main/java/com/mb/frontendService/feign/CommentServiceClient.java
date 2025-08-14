package com.mb.frontendService.feign;

import com.mb.frontendService.dto.CommentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "comment-service")
public interface CommentServiceClient {
    
    @GetMapping("/api/comments/{id}")
    CommentDto getCommentById(@PathVariable Long id);
    
    @GetMapping("/api/comments/post/{postId}")
    List<CommentDto> getCommentsByPostId(@PathVariable Long postId);
    
    @PostMapping("/api/comments")
    void createComment(@RequestBody CommentDto commentDto);
    
    @PutMapping("/api/comments/{id}")
    void updateComment(@PathVariable Long id, @RequestBody CommentDto commentDto);
    
    @DeleteMapping("/api/comments/{id}")
    void deleteComment(@PathVariable Long id);
}
