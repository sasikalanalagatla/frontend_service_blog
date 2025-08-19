package com.mb.frontendService.controller;

import com.mb.frontendService.dto.AuthResponse;
import com.mb.frontendService.dto.LoginRequest;
import com.mb.frontendService.dto.RegisterRequest;
import com.mb.frontendService.feign.UserServiceClient;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/signin")
    public String signInForm(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            logger.info("User already signed in, redirecting to home.");
            return "redirect:/";
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "signin";
    }

    @PostMapping("/signin")
    public String signIn(@ModelAttribute LoginRequest loginRequest,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting login for user: {}", loginRequest.getName());
            AuthResponse authResponse = userServiceClient.login(loginRequest);

            if (authResponse.getUser() != null && authResponse.getToken() != null) {
                session.setAttribute("user", authResponse.getUser());
                session.setAttribute("token", authResponse.getToken());

                logger.info("User {} logged in successfully.", authResponse.getUser().getName());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Welcome back, " + authResponse.getUser().getName() + "!");
                return "redirect:/";
            } else {
                logger.warn("Login failed for user: {}", loginRequest.getName());
                model.addAttribute("errorMessage", "Login failed. Please check your credentials.");
                return "signin";
            }

        } catch (Exception e) {
            logger.error("Error during login for user {}: {}", loginRequest.getName(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "signin";
        }
    }

    @GetMapping("/signup")
    public String signUpForm(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            logger.info("User already signed in, redirecting to home.");
            return "redirect:/";
        }

        model.addAttribute("registerRequest", new RegisterRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute RegisterRequest registerRequest,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting registration for email: {}", registerRequest.getEmail());

            if (registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null ||
                    !registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                logger.warn("Password mismatch during registration for email: {}", registerRequest.getEmail());
                model.addAttribute("error", "Passwords do not match");
                model.addAttribute("name", registerRequest.getName());
                model.addAttribute("email", registerRequest.getEmail());
                return "signup";
            }

            AuthResponse response = userServiceClient.register(registerRequest);

            if (response.getUser() != null && response.getToken() != null) {
                logger.info("Registration successful for user: {}", response.getUser().getName());
                redirectAttributes.addFlashAttribute("success",
                        "🎉 Registration successful! Please sign in with your credentials.");
                return "redirect:/auth/signin";
            } else {
                String errorMsg = response.getMessage() != null ? response.getMessage() : "Registration failed.";
                logger.warn("Registration failed for email {}: {}", registerRequest.getEmail(), errorMsg);
                model.addAttribute("error", errorMsg);
                model.addAttribute("name", registerRequest.getName());
                model.addAttribute("email", registerRequest.getEmail());
                return "signup";
            }
        } catch (Exception e) {
            logger.error("Error during registration for email {}: {}", registerRequest.getEmail(), e.getMessage(), e);
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("name", registerRequest.getName());
            model.addAttribute("email", registerRequest.getEmail());
            return "signup";
        }
    }

    @PostMapping("/signout")
    public String signOut(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String token = (String) session.getAttribute("token");
            if (token != null) {
                logger.info("Signing out user with token: {}", token.substring(0, Math.min(10, token.length())) + "...");
                userServiceClient.logout(token);
            }
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been signed out successfully.");
        return "redirect:/";
    }

    @GetMapping("/signout")
    public String signOutGet(HttpSession session, RedirectAttributes redirectAttributes) {
        return signOut(session, redirectAttributes);
    }

    @GetMapping("/services")
    @ResponseBody
    public String checkServices() {
        logger.info("Checking registered services via DiscoveryClient...");
        StringBuilder sb = new StringBuilder();
        sb.append("=== AVAILABLE SERVICES ===\n");

        var services = discoveryClient.getServices();
        sb.append("Total services: ").append(services.size()).append("\n");

        for (String service : services) {
            var instances = discoveryClient.getInstances(service);
            sb.append("Service: ").append(service).append(" -> ");
            if (instances.isEmpty()) {
                sb.append("NO INSTANCES\n");
            } else {
                for (var instance : instances) {
                    sb.append(instance.getHost()).append(":").append(instance.getPort())
                            .append(" (").append(instance.getUri()).append(")\n");
                }
            }
        }

        return sb.toString();
    }
}