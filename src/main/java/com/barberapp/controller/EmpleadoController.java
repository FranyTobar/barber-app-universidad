package com.barberapp.controller;

import com.barberapp.entity.Usuario;
import com.barberapp.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/empleado")
public class EmpleadoController {
    
    @Autowired(required = false)  // Optional, no falla si no existe
    private UsuarioService usuarioService;
    
    // Dashboard principal del empleado
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email);
        model.addAttribute("nombre", getNombreUsuario(email));
        return "empleado/dashboard";
    }
    
    // Mis Clientes
    @GetMapping("/clientes")
    public String clientes(Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email);
        model.addAttribute("nombre", getNombreUsuario(email));
        return "empleado/clientes";
    }
    
    // Mi Perfil
    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        String email = authentication.getName();
        
        // Intentar obtener datos reales del usuario
        String nombreUsuario = getNombreUsuario(email);
        String nombreMostrar = nombreUsuario;
        
        // Si el servicio está disponible, intentar obtener datos reales
        if (usuarioService != null) {
            try {
                Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
                        nombreMostrar = usuario.getNombre();
                    }
                }
            } catch (Exception e) {
                // Si falla, usar el nombre formateado del email
                System.out.println("Warning: No se pudo obtener datos del usuario: " + e.getMessage());
            }
        }
        
        // Datos para la vista
        model.addAttribute("nombre", nombreMostrar);
        model.addAttribute("email", email);
        model.addAttribute("rol", "BARBERO");
        model.addAttribute("especialidad", "Corte Clásico y Moderno");
        model.addAttribute("calificacion", 4.8);
        model.addAttribute("telefono", "+34 612 345 678");
        model.addAttribute("direccion", "Calle Principal 123, Madrid");
        model.addAttribute("horarioTrabajo", "Lunes a Viernes: 9:00 - 18:00");
        model.addAttribute("citasAtendidas", 24);
        model.addAttribute("ingresosHoy", "$1,850");
        
        return "empleado/perfil";
    }
    
    // Mis Horarios - Temporal
    @GetMapping("/horarios")
    public String horarios(Authentication authentication, Model model) {
        model.addAttribute("email", authentication.getName());
        return "empleado/dashboard";
    }
    
    // Método auxiliar para obtener nombre desde email
    private String getNombreUsuario(String email) {
        try {
            String nombreParte = email.split("@")[0];
            String[] partes = nombreParte.split("\\.");
            StringBuilder nombre = new StringBuilder();
            
            for (String parte : partes) {
                if (!parte.isEmpty()) {
                    nombre.append(parte.substring(0, 1).toUpperCase())
                          .append(parte.substring(1))
                          .append(" ");
                }
            }
            return nombre.toString().trim();
        } catch (Exception e) {
            return "Empleado";
        }
    }
}  