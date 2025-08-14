package com.mb.frontendService.feign;

import com.mb.frontendService.dto.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "post-service")
public interface PostServiceClient {
    
    @GetMapping("/api/posts/{id}")
    PostDto getPostById(@PathVariable Long id);
    
    @GetMapping("/api/posts")
    List<PostDto> getAllPosts();
    
    @GetMapping("/api/posts/published")
    List<PostDto> getPublishedPosts();
    
    @GetMapping("/api/posts/author/name/{author}")
    List<PostDto> getPostsByAuthor(@PathVariable String author);
    
    @GetMapping("/api/posts/tag/{tag}")
    List<PostDto> getPostsByTag(@PathVariable String tag);
    
    @GetMapping("/api/posts/search")
    List<PostDto> searchPosts(@RequestParam String keyword);
    
    @GetMapping("/api/posts/tags")
    List<String> getAllTags();

    @PostMapping("/api/posts")
    void createPost(@RequestBody PostDto postDto);

    @PutMapping("/api/posts/{id}")
    PostDto updatePost(@PathVariable Long id, @RequestBody PostDto postDto);

    @DeleteMapping("/api/posts/{id}")
    void deletePost(@PathVariable Long id);

}
