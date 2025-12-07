package com.barberapp.controller;

import com.barberapp.dto.RegistroRequest;
import com.barberapp.entity.Cliente;
import com.barberapp.entity.Empleado;
import com.barberapp.entity.Rol;
import com.barberapp.entity.Usuario;
import com.barberapp.service.ClienteService;
import com.barberapp.service.EmpleadoService;
import com.barberapp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registroRequest", new RegistroRequest());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegistroRequest registroRequest, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
        
        System.out.println("=== 🔐 REGISTRO DE USUARIO ===");
        System.out.println("Nombre: " + registroRequest.getNombre());
        System.out.println("Email: " + registroRequest.getEmail());
        System.out.println("Rol: " + registroRequest.getRol());
        
        // Validaciones básicas
        if (registroRequest.getPassword() == null || registroRequest.getConfirmPassword() == null) {
            model.addAttribute("error", "Las contraseñas son requeridas");
            return "auth/register";
        }
        
        if (!registroRequest.getPassword().equals(registroRequest.getConfirmPassword())) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "auth/register";
        }
        
        if (registroRequest.getRol() == null) {
            model.addAttribute("error", "Debe seleccionar un tipo de usuario");
            return "auth/register";
        }
        
        if (usuarioService.existeEmail(registroRequest.getEmail())) {
            model.addAttribute("error", "El email ya está registrado");
            return "auth/register";
        }
        
        try {
            // DEBUG: Mostrar contraseña antes y después de encriptar
            System.out.println("=== 🔐 DEBUG REGISTRO ===");
            System.out.println("Contraseña original: " + registroRequest.getPassword());
            
            String passwordEncriptada = passwordEncoder.encode(registroRequest.getPassword());
            System.out.println("Contraseña encriptada: " + passwordEncriptada);
            System.out.println("Longitud encriptada: " + passwordEncriptada.length());
            
            // Verificar que se puede validar
            boolean matches = passwordEncoder.matches(registroRequest.getPassword(), passwordEncriptada);
            System.out.println("¿PasswordEncoder puede validarla? " + matches);
            
            if (registroRequest.getRol() == Rol.CLIENTE) {
                System.out.println("Creando cliente...");
                // Crear cliente
                Cliente cliente = new Cliente();
                cliente.setNombre(registroRequest.getNombre());
                cliente.setEmail(registroRequest.getEmail());
                cliente.setTelefono(registroRequest.getTelefono());
                cliente.setPassword(passwordEncriptada);
                cliente.setRol(Rol.CLIENTE);
                cliente.setActivo(true);
                
                clienteService.crearCliente(cliente);
                System.out.println("✅ Cliente creado exitosamente");
                
            } else if (registroRequest.getRol() == Rol.BARBERO) {
                System.out.println("Creando barbero...");
                // Crear empleado
                Empleado empleado = new Empleado();
                empleado.setNombre(registroRequest.getNombre());
                empleado.setEmail(registroRequest.getEmail());
                empleado.setTelefono(registroRequest.getTelefono());
                empleado.setPassword(passwordEncriptada);
                empleado.setRol(Rol.BARBERO);
                empleado.setEspecialidad("CORTE");
                empleado.setCalificacionPromedio(new java.math.BigDecimal("4.5"));
                empleado.setHorarioTrabajo("COMPLETO");
                empleado.setActivo(true);
                
                empleadoService.crearEmpleado(empleado);
                System.out.println("✅ Barbero creado exitosamente");
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Registro exitoso! Ahora puedes iniciar sesión con tu email: " + registroRequest.getEmail());
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            System.err.println("❌ Error en registro: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en el registro: " + e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("message", "Has cerrado sesión exitosamente.");
        return "home";
    }
    
    @GetMapping("/test-password")
    @ResponseBody
    public String testPassword(@RequestParam(required = false) String email) {
        StringBuilder result = new StringBuilder();
        result.append("<h3>🔧 TEST PASSWORD ENCODER</h3>");
        
        try {
            // 1. Test básico del encoder
            String testPass = "Test123";
            String encoded = passwordEncoder.encode(testPass);
            boolean matches = passwordEncoder.matches(testPass, encoded);
            
            result.append("<b>Test básico:</b><br>");
            result.append("  • Password: ").append(testPass).append("<br>");
            result.append("  • Encoded: ").append(encoded.substring(0, 30)).append("...<br>");
            result.append("  • ¿Matches? ").append(matches ? "✅ SÍ" : "❌ NO").append("<br><br>");
            
            // 2. Si se proporciona email, probar ese usuario específico
            if (email != null && !email.isEmpty()) {
                result.append("<b>Test usuario específico:</b> ").append(email).append("<br>");
                
                try {
                    var usuarioOpt = usuarioService.buscarPorEmail(email);
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        result.append("  • Usuario encontrado: ✅<br>");
                        result.append("  • Nombre: ").append(usuario.getNombre()).append("<br>");
                        result.append("  • Rol: ").append(usuario.getRol()).append("<br>");
                        result.append("  • Password en BD: ").append(usuario.getPassword() != null ? 
                            usuario.getPassword().substring(0, Math.min(30, usuario.getPassword().length())) + "..." : "NULL").append("<br>");
                        
                        // Probar contraseña
                        boolean userMatches = passwordEncoder.matches("cliente123", usuario.getPassword());
                        result.append("  • ¿Password 'cliente123' matches? ").append(userMatches ? "✅ SÍ" : "❌ NO").append("<br>");
                    } else {
                        result.append("  • Usuario NO encontrado: ❌<br>");
                    }
                } catch (Exception e) {
                    result.append("  • Error: ").append(e.getMessage()).append("<br>");
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/test-db")
    @ResponseBody
    public String testDatabase() {
        StringBuilder result = new StringBuilder();
        result.append("<h3>🔍 DIAGNÓSTICO BASE DE DATOS</h3>");
        
        try {
            // 1. Contar usuarios - No podemos contar directamente, mostrar mensaje
            result.append("<b>Estado de la base de datos:</b><br>");
            result.append("  • Para ver usuarios: Accede a <a href='http://localhost:8080/h2-console' target='_blank'>H2 Console</a><br>");
            result.append("  • JDBC URL: jdbc:h2:mem:barberdb<br>");
            result.append("  • Usuario: sa (sin contraseña)<br><br>");
            
            // 2. Probar PasswordEncoder
            result.append("<b>Test PasswordEncoder:</b><br>");
            String testPass = "Test12345";
            String encoded = passwordEncoder.encode(testPass);
            boolean matches = passwordEncoder.matches(testPass, encoded);
            result.append("  • Encode 'Test12345': ").append(encoded.substring(0, 30)).append("...<br>");
            result.append("  • ¿Matches? ").append(matches ? "✅ SÍ" : "❌ NO").append("<br>");
            
            // 3. Prueba de servicio UsuarioService
            result.append("<br><b>Test UsuarioService:</b><br>");
            boolean testEmailExists = usuarioService.existeEmail("cliente@barberapp.com");
            result.append("  • ¿Existe 'cliente@barberapp.com'? ").append(testEmailExists ? "✅ SÍ" : "❌ NO").append("<br>");
            
            boolean testEmailNotExists = usuarioService.existeEmail("noexiste@test.com");
            result.append("  • ¿Existe 'noexiste@test.com'? ").append(testEmailNotExists ? "✅ SÍ (raro)" : "✅ NO (correcto)").append("<br>");
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
} 