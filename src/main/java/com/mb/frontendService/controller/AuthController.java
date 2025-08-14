package com.mb.frontendService.controller;

import com.mb.frontendService.dto.AuthResponse;
import com.mb.frontendService.dto.LoginRequest;
import com.mb.frontendService.dto.RegisterRequest;
import com.mb.frontendService.feign.UserServiceClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.cloud.client.discovery.DiscoveryClient;


@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/signin")
    public String signInForm(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
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
            AuthResponse authResponse = userServiceClient.login(loginRequest);

            if (authResponse.getUser() != null && authResponse.getToken() != null) {
                session.setAttribute("user", authResponse.getUser());
                session.setAttribute("token", authResponse.getToken());

                redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + authResponse.getUser().getName() + "!");  // ✅ Use getName() only
                return "redirect:/";
            } else {
                model.addAttribute("errorMessage", "Login failed. Please check your credentials.");
                return "signin";
            }

        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "signin";
        }
    }

    @GetMapping("/signup")
    public String signUpForm(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
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
            if (registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null ||
                !registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                model.addAttribute("error", "Passwords do not match");
                model.addAttribute("name", registerRequest.getName());  // ✅ Changed from username
                model.addAttribute("email", registerRequest.getEmail());
                return "signup";
            }

            AuthResponse response = userServiceClient.register(registerRequest);

            if (response.getUser() != null && response.getToken() != null) {
                redirectAttributes.addFlashAttribute("success", "🎉 Registration successful! Your account has been created. Please sign in with your credentials.");
                return "redirect:/auth/signin";
            } else {
                String errorMsg = response.getMessage() != null ? response.getMessage() : "Registration failed. Please try again.";
                model.addAttribute("error", errorMsg);
                model.addAttribute("name", registerRequest.getName());  // ✅ Changed from username
                model.addAttribute("email", registerRequest.getEmail());
                return "signup";
            }
        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("name", registerRequest.getName());  // ✅ Changed from username
            model.addAttribute("email", registerRequest.getEmail());
            return "signup";
        }
    }

    @PostMapping("/signout")
    public String signOut(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String token = (String) session.getAttribute("token");
            if (token != null) {
                userServiceClient.logout(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
