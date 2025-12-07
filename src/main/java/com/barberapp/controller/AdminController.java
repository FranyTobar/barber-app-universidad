package com.barberapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        return "admin/dashboard";
    }
    
    @GetMapping("/empleados")
    public String empleados(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Empleados");
        return "admin/empleados";
    }
    
    @GetMapping("/clientes")
    public String clientes(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Clientes");
        return "admin/clientes";
    }
    
    @GetMapping("/citas")
    public String todasCitas(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Todas las Citas");
        return "admin/citas";
    }
    
    @GetMapping("/servicios")
    public String servicios(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Servicios");
        return "admin/servicios";
    }
    
    @GetMapping("/reportes")
    public String reportes(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Reportes");
        return "admin/reportes";
    }
    
    @GetMapping("/configuracion")
    public String configuracion(Authentication authentication, Model model) {
        model.addAttribute("nombre", authentication.getName());
        model.addAttribute("pagina", "Configuración");
        return "admin/configuracion";
    }
} 