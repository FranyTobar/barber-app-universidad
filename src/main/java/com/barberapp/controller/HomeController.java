package com.barberapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // DEBUG: Mostrar información del usuario
        System.out.println("=== 🔍 REDIRECCIÓN DASHBOARD ===");
        System.out.println("Usuario: " + (auth != null ? auth.getName() : "null"));
        System.out.println("Autenticado: " + (auth != null ? auth.isAuthenticated() : "false"));
        
        // Si no está autenticado, redirigir al login
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            System.out.println("❌ No autenticado - Redirigiendo a login");
            return "redirect:/auth/login";
        }
        
        // Redirigir según el rol del usuario
        boolean isBarbero = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_BARBERO"));
        
        boolean isCliente = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        System.out.println("Roles detectados:");
        System.out.println("  - ROLE_BARBERO: " + isBarbero);
        System.out.println("  - ROLE_CLIENTE: " + isCliente);
        System.out.println("  - ROLE_ADMIN: " + isAdmin);
        
        // Redirigir según rol
        if (isBarbero) {
            System.out.println("✅ Redirigiendo a: /empleado/dashboard");
            return "redirect:/empleado/dashboard";
        } else if (isCliente) {
            System.out.println("✅ Redirigiendo a: /cliente/dashboard");
            return "redirect:/cliente/dashboard";
        } else if (isAdmin) {
            System.out.println("✅ Redirigiendo a: /admin/dashboard");
            return "redirect:/admin/dashboard";
        }
        
        // Si no tiene un rol específico, mostrar dashboard general
        System.out.println("⚠️  No tiene rol específico - Mostrando dashboard general");
        if (auth != null) {
            model.addAttribute("username", auth.getName());
        }
        return "dashboard";
    }
    
    // ⚠️ ¡ELIMINA ESTE MÉTODO! Ahora está en AdminController.java
    // @GetMapping("/admin/dashboard")
    // public String showAdminDashboard(Model model) {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth != null && auth.isAuthenticated()) {
    //         model.addAttribute("username", auth.getName());
    //     }
    //     return "admin/dashboard";
    // }
} 