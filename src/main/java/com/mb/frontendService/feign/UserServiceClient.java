package com.mb.frontendService.feign;

import com.mb.frontendService.dto.AuthResponse;
import com.mb.frontendService.dto.LoginRequest;
import com.mb.frontendService.dto.RegisterRequest;
import com.mb.frontendService.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);
    
    @GetMapping("/api/users/username/{username}")
    UserDto getUserByUsername(@PathVariable String username);
    
    @PostMapping("/api/auth/register")
    AuthResponse register(@RequestBody RegisterRequest registerRequest);
    
    @PostMapping("/api/auth/login")
    AuthResponse login(@RequestBody LoginRequest loginRequest);
    
    @PostMapping("/api/auth/logout")
    void logout(@RequestParam String token);
}
