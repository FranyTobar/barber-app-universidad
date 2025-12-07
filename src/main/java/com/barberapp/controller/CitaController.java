package com.barberapp.controller;

import com.barberapp.dto.CitaRequest;
import com.barberapp.entity.*;
import com.barberapp.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/citas")
public class CitaController {
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private ServicioService servicioService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    // ========== DIAGNÓSTICO ==========
    
    @GetMapping("/debug/empleados")
    @ResponseBody
    public String debugEmpleados() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🔍 Diagnóstico Empleados</h1>");
        
        try {
            // Listar todos los empleados
            List<Empleado> empleados = empleadoService.listarActivos();
            sb.append("<h2>Empleados activos (").append(empleados.size()).append("):</h2>");
            sb.append("<table border='1'><tr><th>ID</th><th>Nombre</th><th>Email</th><th>Especialidad</th></tr>");
            
            for (Empleado e : empleados) {
                sb.append("<tr>")
                  .append("<td>").append(e.getId()).append("</td>")
                  .append("<td>").append(e.getNombre()).append("</td>")
                  .append("<td>").append(e.getEmail()).append("</td>")
                  .append("<td>").append(e.getEspecialidad()).append("</td>")
                  .append("</tr>");
            }
            sb.append("</table>");
            
            // Probar buscar por ID
            sb.append("<h2>Prueba buscar por ID (1-10):</h2>");
            for (long id = 1; id <= 10; id++) {
                Optional<Empleado> emp = empleadoService.buscarPorId(id);
                sb.append("<p>ID ").append(id).append(": ");
                if (emp.isPresent()) {
                    sb.append("<span style='color:green'>ENCONTRADO - ").append(emp.get().getNombre())
                      .append(" (Especialidad: ").append(emp.get().getEspecialidad()).append(")</span>");
                } else {
                    sb.append("<span style='color:red'>NO encontrado</span>");
                }
                sb.append("</p>");
            }
            
        } catch (Exception e) {
            sb.append("<p style='color:red'>Error: ").append(e.getMessage()).append("</p>");
            e.printStackTrace();
        }
        
        return sb.toString();
    }
    
    // ========== CLIENTE ==========
    
    @GetMapping("/cliente")
    public String listarCitasCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        System.out.println("=== 📋 LISTAR CITAS CLIENTE (FIX) ===");
        System.out.println("Email autenticado: " + email);
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("✅ Cliente encontrado: " + cliente.getNombre() + " (ID: " + cliente.getId() + ")");
            
            // 🔧 USAR EL MÉTODO CORRECTO DEL SERVICIO (no filtrar manualmente)
            System.out.println("🔍 Usando citaService.listarPorCliente()...");
            List<Cita> citasCliente = citaService.listarPorCliente(cliente);
            
            // Ordenar por fecha más reciente primero
            citasCliente.sort((c1, c2) -> c2.getFechaHora().compareTo(c1.getFechaHora()));
            
            System.out.println("📊 Total citas del cliente " + cliente.getId() + ": " + citasCliente.size());
            
            // Mostrar cada cita para debug
            for (Cita cita : citasCliente) {
                System.out.println("   📅 Cita ID: " + cita.getId() + 
                                 " | Servicio: " + (cita.getServicio() != null ? cita.getServicio().getNombre() : "null") +
                                 " | Estado: " + cita.getEstado() +
                                 " | Fecha: " + cita.getFechaHora());
            }
            
            model.addAttribute("citas", citasCliente);
            model.addAttribute("cliente", cliente);
            return "cliente/citas";
        } else {
            System.out.println("❌ Cliente no encontrado para email: " + email);
            return "redirect:/auth/login";
        }
    }
    
    // ========== ENDPOINTS DE DIAGNÓSTICO ADICIONALES ==========
    
    @GetMapping("/cliente/debug")
    @ResponseBody
    public String debugCitasCliente() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🔍 Diagnóstico de Citas del Cliente</h1>");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            sb.append("<p><strong>Email autenticado:</strong> ").append(email).append("</p>");
            
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                sb.append("<h2>Cliente: ").append(cliente.getNombre())
                  .append(" (ID: ").append(cliente.getId()).append(")</h2>");
                
                // Método 1: Usando el servicio
                List<Cita> citasServicio = citaService.listarTodas();
                sb.append("<h3>📊 Método 1: citaService.listarTodas()</h3>");
                sb.append("<p><strong>Total citas en BD:</strong> ").append(citasServicio.size()).append("</p>");
                sb.append("<table border='1'><tr><th>ID</th><th>Cliente ID</th><th>Empleado ID</th><th>Servicio ID</th><th>Fecha</th><th>Estado</th></tr>");
                
                for (Cita c : citasServicio) {
                    sb.append("<tr>")
                      .append("<td>").append(c.getId()).append("</td>")
                      .append("<td>").append(c.getCliente() != null ? c.getCliente().getId() : "null").append("</td>")
                      .append("<td>").append(c.getEmpleado() != null ? c.getEmpleado().getId() : "null").append("</td>")
                      .append("<td>").append(c.getServicio() != null ? c.getServicio().getId() : "null").append("</td>")
                      .append("<td>").append(c.getFechaHora()).append("</td>")
                      .append("<td>").append(c.getEstado()).append("</td>")
                      .append("</tr>");
                }
                sb.append("</table>");
                
                // Método 2: Usando repositorio directamente
                sb.append("<h3>📊 Método 2: citaService.listarPorCliente()</h3>");
                List<Cita> citasCliente = citaService.listarPorCliente(cliente);
                
                sb.append("<p><strong>Citas del cliente ").append(cliente.getId()).append(":</strong> ").append(citasCliente.size()).append("</p>");
                sb.append("<ul>");
                for (Cita c : citasCliente) {
                    sb.append("<li>ID: ").append(c.getId())
                      .append(" | Servicio: ").append(c.getServicio() != null ? c.getServicio().getNombre() : "null")
                      .append(" | Estado: ").append(c.getEstado())
                      .append(" | Fecha: ").append(c.getFechaHora())
                      .append("</li>");
                }
                sb.append("</ul>");
                
            } else {
                sb.append("<p style='color:red'>Cliente no encontrado</p>");
            }
            
        } catch (Exception e) {
            sb.append("<p style='color:red'>Error: ").append(e.getMessage()).append("</p>");
            e.printStackTrace();
        }
        
        return sb.toString();
    }
    
    @GetMapping("/cliente/force-reload")
    public String forceReload(RedirectAttributes redirectAttributes) {
        try {
            // Limpiar caché forzando una consulta
            List<Cita> todas = citaService.listarTodas();
            redirectAttributes.addFlashAttribute("success", 
                "Recarga forzada completada. Total citas en BD: " + todas.size());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        return "redirect:/citas/cliente";
    }
    
    @GetMapping("/cliente/nueva")
    public String mostrarFormularioNuevaCita(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("📋 Formulario nueva cita - Cliente: " + cliente.getNombre() + " (ID: " + cliente.getId() + ")");
            
            model.addAttribute("citaRequest", new CitaRequest());
            model.addAttribute("cliente", cliente);
            
            // Obtener empleados y servicios disponibles
            List<Empleado> empleados = empleadoService.listarActivos();
            List<Servicio> servicios = servicioService.listarActivos();
            
            System.out.println("📋 Empleados disponibles:");
            for (Empleado emp : empleados) {
                System.out.println("   👨‍💼 ID: " + emp.getId() + " | Nombre: " + emp.getNombre() + 
                                 " | Especialidad: " + emp.getEspecialidad());
            }
            
            model.addAttribute("empleados", empleados);
            model.addAttribute("servicios", servicios);
            
            return "cliente/nueva-cita";
        }
        
        return "redirect:/auth/login";
    }
    
    @PostMapping("/cliente/crear")
    public String crearCita(@Valid @ModelAttribute CitaRequest citaRequest,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        System.out.println("=== 🎯 CitaController.crearCita() INICIADO ===");
        System.out.println("📝 Datos recibidos del formulario:");
        System.out.println("   Empleado ID: " + citaRequest.getEmpleadoId());
        System.out.println("   Servicio ID: " + citaRequest.getServicioId());
        System.out.println("   Fecha/Hora: " + citaRequest.getFechaHora());
        System.out.println("   Notas: " + citaRequest.getNotas());
        System.out.println("   ¿Fecha/Hora null?: " + (citaRequest.getFechaHora() == null));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("   Cliente (email): " + email);
        
        try {
            System.out.println("🔍 Buscando cliente...");
            Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
            System.out.println("   Cliente encontrado: " + clienteOpt.isPresent());
            if (clienteOpt.isPresent()) {
                System.out.println("   Cliente ID: " + clienteOpt.get().getId() + 
                                 ", Nombre: " + clienteOpt.get().getNombre());
            }
            
            System.out.println("🔍 Buscando empleado con ID: " + citaRequest.getEmpleadoId());
            Optional<Empleado> empleadoOpt = empleadoService.buscarPorId(citaRequest.getEmpleadoId());
            System.out.println("   Empleado encontrado: " + empleadoOpt.isPresent());
            if (empleadoOpt.isPresent()) {
                System.out.println("   Empleado ID: " + empleadoOpt.get().getId() + 
                                 ", Nombre: " + empleadoOpt.get().getNombre() +
                                 ", Especialidad: " + empleadoOpt.get().getEspecialidad());
            } else {
                System.out.println("❌ ERROR: No se encontró empleado con ID " + citaRequest.getEmpleadoId());
                System.out.println("   IDs válidos de barberos: 5 (Carlos), 6 (Ana), 7 (Roberto)");
            }
            
            System.out.println("🔍 Buscando servicio con ID: " + citaRequest.getServicioId());
            Optional<Servicio> servicioOpt = servicioService.buscarPorId(citaRequest.getServicioId());
            System.out.println("   Servicio encontrado: " + servicioOpt.isPresent());
            if (servicioOpt.isPresent()) {
                System.out.println("   Servicio ID: " + servicioOpt.get().getId() + 
                                 ", Nombre: " + servicioOpt.get().getNombre() +
                                 ", Precio: $" + servicioOpt.get().getPrecio());
            }
            
            if (clienteOpt.isPresent() && empleadoOpt.isPresent() && servicioOpt.isPresent()) {
                System.out.println("✅ Todos los datos encontrados, creando cita...");
                
                Cita cita = new Cita();
                cita.setCliente(clienteOpt.get());
                cita.setEmpleado(empleadoOpt.get());
                cita.setServicio(servicioOpt.get());
                cita.setFechaHora(citaRequest.getFechaHora());
                cita.setNotas(citaRequest.getNotas());
                cita.setEstado(EstadoCita.PENDIENTE);
                
                System.out.println("📅 Objeto Cita creado:");
                System.out.println("   Cliente: " + cita.getCliente().getNombre());
                System.out.println("   Empleado: " + cita.getEmpleado().getNombre());
                System.out.println("   Servicio: " + cita.getServicio().getNombre());
                System.out.println("   Fecha/Hora: " + cita.getFechaHora());
                System.out.println("   Estado: " + cita.getEstado());
                
                Cita citaGuardada = citaService.crearCita(cita);
                System.out.println("🎉 CITA CREADA EXITOSAMENTE - ID: " + citaGuardada.getId());
                
                // 🔧 AGREGADO: Forzar recarga inmediata
                System.out.println("🔄 Forzando recarga de citas del cliente...");
                List<Cita> citasActualizadas = citaService.listarPorCliente(clienteOpt.get());
                System.out.println("✅ Citas actualizadas: " + citasActualizadas.size());
                
                redirectAttributes.addFlashAttribute("success", 
                    "Cita creada exitosamente! ID: " + citaGuardada.getId() + 
                    ". Ahora tienes " + citasActualizadas.size() + " citas.");
                return "redirect:/citas/cliente";
                
            } else {
                System.out.println("❌ Error: Datos no válidos");
                System.out.println("   Cliente: " + clienteOpt.isPresent());
                System.out.println("   Empleado: " + empleadoOpt.isPresent());
                System.out.println("   Servicio: " + servicioOpt.isPresent());
                
                String errorMsg = "Error: Datos no válidos. ";
                if (!empleadoOpt.isPresent()) {
                    errorMsg += "El barbero seleccionado no existe. ";
                    errorMsg += "IDs válidos: 5 (Carlos), 6 (Ana), 7 (Roberto). ";
                }
                
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/citas/cliente/nueva";
            }
            
        } catch (Exception e) {
            System.out.println("💥 ERROR AL CREAR CITA: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear cita: " + e.getMessage() + ". Por favor intente con otra fecha/hora.");
            return "redirect:/citas/cliente/nueva";
        }
    }
    
    @GetMapping("/cliente/cancelar/{id}")
    public String cancelarCitaCliente(@PathVariable Long id, 
                                     @RequestParam(required = false) String motivo,
                                     RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Optional<Cita> citaOpt = citaService.buscarPorId(id);
            
            if (citaOpt.isPresent() && citaOpt.get().getCliente().equals(clienteOpt.get())) {
                citaService.cancelarCita(id, motivo);
                redirectAttributes.addFlashAttribute("success", "Cita cancelada exitosamente");
            }
        }
        
        return "redirect:/citas/cliente";
    }
    
    // ========== EMPLEADO ==========
    
    @GetMapping("/empleado")
    public String listarCitasEmpleado(Model model,
                                     @RequestParam(required = false) String estado) {
        
        System.out.println("=== 🚀 CITAS REALES PARA EMPLEADO ===");
        System.out.println("Accediendo a /citas/empleado");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Empleado autenticado: " + email);
        System.out.println("Fecha actual: " + LocalDateTime.now());
        
        Optional<Empleado> empleadoOpt = empleadoService.buscarPorEmail(email);
        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();
            System.out.println("✅ Empleado encontrado: " + empleado.getNombre() + " (ID: " + empleado.getId() + ")");
            
            // Obtener TODAS las citas del empleado (usando el método que SÍ existe)
            List<Cita> todasCitas = citaService.listarPorEmpleado(empleado);
            System.out.println("📊 Total citas encontradas: " + todasCitas.size());
            
            // Filtrar por estado si se especifica (usando filter de stream)
            List<Cita> citasFiltradas;
            if (estado != null && !estado.isEmpty()) {
                System.out.println("Filtrando por estado: " + estado);
                try {
                    EstadoCita estadoCita = EstadoCita.valueOf(estado.toUpperCase());
                    citasFiltradas = todasCitas.stream()
                        .filter(c -> c.getEstado() == estadoCita)
                        .collect(Collectors.toList());
                    System.out.println("Citas después de filtrar por estado '" + estado + "': " + citasFiltradas.size());
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Estado no válido: " + estado + ". Mostrando todas las citas.");
                    citasFiltradas = todasCitas;
                }
            } else {
                citasFiltradas = todasCitas;
            }
            
            // Diagnóstico detallado
            System.out.println("=== 📊 DIAGNÓSTICO DE CITAS ===");
            for (Cita cita : todasCitas) {
                boolean esFutura = cita.getFechaHora().isAfter(LocalDateTime.now());
                System.out.println(String.format(
                    "   📅 Cita ID: %d | Fecha: %s | Cliente: %s | Estado: %s | Futura: %s",
                    cita.getId(),
                    cita.getFechaHora(),
                    cita.getCliente().getNombre(),
                    cita.getEstado(),
                    esFutura ? "✅ SÍ" : "❌ NO"
                ));
            }
            
            // Filtrar para mostrar (últimos 60 días + futuros 90 días)
            LocalDateTime inicio = LocalDateTime.now().minusDays(60);
            LocalDateTime fin = LocalDateTime.now().plusDays(90);
            
            List<Cita> citasParaMostrar = citasFiltradas.stream()
                .filter(c -> !c.getFechaHora().isBefore(inicio) && !c.getFechaHora().isAfter(fin))
                .sorted((c1, c2) -> c2.getFechaHora().compareTo(c1.getFechaHora())) // Más recientes primero
                .collect(Collectors.toList());
            
            System.out.println("📊 Citas para mostrar (últimos 60 días + próximos 90 días): " + citasParaMostrar.size());
            
            // Obtener citas de hoy para estadísticas (usando el método que SÍ existe)
            List<Cita> citasHoy = citaService.listarCitasEmpleadoHoy(empleado);
            System.out.println("📅 Citas de hoy: " + citasHoy.size());
            
            // Pasar datos al template
            model.addAttribute("citas", citasParaMostrar);
            model.addAttribute("citasHoy", citasHoy);
            model.addAttribute("empleado", empleado);
            model.addAttribute("totalCitasEnBD", todasCitas.size());
            model.addAttribute("diagnostico", true); // Para mostrar panel de diagnóstico
            
            return "empleado/citas";
        } else {
            System.out.println("❌ Empleado no encontrado para email: " + email);
            return "redirect:/auth/login";
        }
    }
    
    @GetMapping("/empleado/hoy")
    public String listarCitasEmpleadoHoy(Model model) {
        System.out.println("=== 📅 CITAS DE HOY PARA EMPLEADO ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Empleado: " + email);
        
        Optional<Empleado> empleadoOpt = empleadoService.buscarPorEmail(email);
        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();
            System.out.println("✅ Empleado: " + empleado.getNombre());
            
            // Obtener citas de hoy
            List<Cita> citasHoy = citaService.listarCitasEmpleadoHoy(empleado);
            
            System.out.println("📊 Citas de hoy encontradas: " + citasHoy.size());
            
            // Diagnóstico
            for (Cita cita : citasHoy) {
                System.out.println(String.format(
                    "   📅 Cita ID: %d | Cliente: %s | Servicio: %s | Hora: %s | Estado: %s",
                    cita.getId(),
                    cita.getCliente().getNombre(),
                    cita.getServicio().getNombre(),
                    cita.getFechaHora().toLocalTime(),
                    cita.getEstado()
                ));
            }
            
            model.addAttribute("empleado", empleado);
            model.addAttribute("citas", citasHoy);
            model.addAttribute("hoy", LocalDate.now());
            
            return "empleado/citas-hoy";
        }
        
        System.out.println("❌ Empleado no encontrado para email: " + email);
        return "redirect:/auth/login";
    }
    
    // ========== ENDPOINT TEMPORAL PARA PROBAR ==========
    
    @GetMapping("/empleado/todas")
    public String listarCitasEmpleadoTodas(Model model) {
        System.out.println("🔧 [TEMPORAL-FIX] Accediendo a /citas/empleado/todas");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Empleado> empleadoOpt = empleadoService.buscarPorEmail(email);
        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();
            List<Cita> citas = citaService.listarPorEmpleado(empleado);
            
            System.out.println("✅ [TEMPORAL-FIX] Empleado: " + empleado.getNombre());
            System.out.println("✅ [TEMPORAL-FIX] Total citas encontradas: " + citas.size());
            
            model.addAttribute("empleado", empleado);
            model.addAttribute("citas", citas);
            model.addAttribute("hoy", LocalDate.now());
            
            return "empleado/citas";
        }
        
        System.out.println("❌ [TEMPORAL-FIX] Empleado no encontrado para email: " + email);
        return "redirect:/empleado/dashboard?error=empleado_no_encontrado";
    }
    
    @GetMapping("/empleado/confirmar/{id}")
    public String confirmarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("=== ✅ CONFIRMAR CITA ===");
        System.out.println("Cita ID: " + id);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Empleado: " + email);
        
        Optional<Empleado> empleadoOpt = empleadoService.buscarPorEmail(email);
        if (empleadoOpt.isPresent()) {
            Optional<Cita> citaOpt = citaService.buscarPorId(id);
            
            if (citaOpt.isPresent() && citaOpt.get().getEmpleado().equals(empleadoOpt.get())) {
                citaService.confirmarCita(id);
                System.out.println("✅ Cita ID " + id + " confirmada por " + empleadoOpt.get().getNombre());
                redirectAttributes.addFlashAttribute("success", "Cita confirmada exitosamente");
            } else {
                System.out.println("❌ Cita no encontrada o no pertenece al empleado");
            }
        } else {
            System.out.println("❌ Empleado no encontrado");
        }
        
        return "redirect:/citas/empleado";
    }
    
    @GetMapping("/empleado/completar/{id}")
    public String completarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        System.out.println("=== ✅ COMPLETAR CITA ===");
        System.out.println("Cita ID: " + id);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        System.out.println("Empleado: " + email);
        
        Optional<Empleado> empleadoOpt = empleadoService.buscarPorEmail(email);
        if (empleadoOpt.isPresent()) {
            Optional<Cita> citaOpt = citaService.buscarPorId(id);
            
            if (citaOpt.isPresent() && citaOpt.get().getEmpleado().equals(empleadoOpt.get())) {
                citaService.completarCita(id);
                System.out.println("✅ Cita ID " + id + " completada por " + empleadoOpt.get().getNombre());
                redirectAttributes.addFlashAttribute("success", "Cita marcada como completada");
            } else {
                System.out.println("❌ Cita no encontrada o no pertenece al empleado");
            }
        } else {
            System.out.println("❌ Empleado no encontrado");
        }
        
        return "redirect:/citas/empleado";
    }
    
    // ========== ADMIN ==========
    
    @GetMapping("/admin")
    public String listarTodasCitas(Model model,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String fecha) {
        
        List<Cita> citas;
        
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoCita estadoCita = EstadoCita.valueOf(estado.toUpperCase());
                citas = citaService.listarPorEstado(estadoCita);
            } catch (IllegalArgumentException e) {
                citas = citaService.listarTodas();
            }
        } else {
            citas = citaService.listarTodas();
        }
        
        model.addAttribute("citas", citas);
        model.addAttribute("estados", EstadoCita.values());
        
        // Estadísticas
        long totalCitas = citas.size();
        long pendientes = citaService.contarCitasPorEstado(EstadoCita.PENDIENTE);
        long confirmadas = citaService.contarCitasPorEstado(EstadoCita.CONFIRMADA);
        long completadas = citaService.contarCitasPorEstado(EstadoCita.COMPLETADA);
        
        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("confirmadas", confirmadas);
        model.addAttribute("completadas", completadas);
        
        return "admin/citas";
    }
    
    @GetMapping("/admin/ver/{id}")
    public String verDetalleCitaAdmin(@PathVariable Long id, Model model) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isPresent()) {
            model.addAttribute("cita", citaOpt.get());
            return "admin/detalle-cita";
        }
        
        return "redirect:/citas/admin";
    }
    
    @GetMapping("/admin/cancelar/{id}")
    public String cancelarCitaAdmin(@PathVariable Long id,
                                   @RequestParam(required = false) String motivo,
                                   RedirectAttributes redirectAttributes) {
        
        citaService.cancelarCita(id, motivo);
        redirectAttributes.addFlashAttribute("success", "Cita cancelada por administrador");
        
        return "redirect:/citas/admin";
    }
    
    // ========== FUNCIONES COMUNES ==========
    
    @GetMapping("/detalle/{id}")
    public String verDetalleCita(@PathVariable Long id, Model model) {
        Optional<Cita> citaOpt = citaService.buscarPorId(id);
        
        if (citaOpt.isPresent()) {
            model.addAttribute("cita", citaOpt.get());
            
            // Verificar permisos
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                Cita cita = citaOpt.get();
                
                boolean puedeVer = false;
                
                if (usuario.getRol() == Rol.ADMIN) {
                    puedeVer = true;
                } else if (usuario.getRol() == Rol.CLIENTE && cita.getCliente().equals(usuario)) {
                    puedeVer = true;
                } else if (usuario.getRol() == Rol.BARBERO && cita.getEmpleado().equals(usuario)) {
                    puedeVer = true;
                }
                
                if (puedeVer) {
                    return "cita/detalle";
                }
            }
        }
        
        return "redirect:/dashboard";
    }
    
    // ========== API para verificar disponibilidad ==========
    
    @GetMapping("/disponibilidad")
    @ResponseBody
    public String verificarDisponibilidad(@RequestParam Long empleadoId,
                                         @RequestParam String fechaHora,
                                         @RequestParam Integer duracion) {
        
        try {
            Optional<Empleado> empleadoOpt = empleadoService.buscarPorId(empleadoId);
            LocalDateTime fecha = LocalDateTime.parse(fechaHora);
            
            if (empleadoOpt.isPresent()) {
                boolean disponible = citaService.verificarDisponibilidad(
                    empleadoOpt.get(), fecha, duracion
                );
                
                return disponible ? "DISPONIBLE" : "NO_DISPONIBLE";
            }
            
            return "ERROR";
            
        } catch (Exception e) {
            return "ERROR";
        }
    }
    
    // ========== MÉTODO DE DIAGNÓSTICO PARA VER TODAS LAS CITAS ==========
    
    @GetMapping("/debug/todas")
    @ResponseBody
    public String debugTodasCitas() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🔍 DIAGNÓSTICO COMPLETO DE CITAS</h1>");
        sb.append("<p><strong>Fecha actual:</strong> ").append(LocalDateTime.now()).append("</p>");
        
        try {
            // Obtener todas las citas
            List<Cita> todasCitas = citaService.listarTodas();
            sb.append("<h2>Total citas en BD: ").append(todasCitas.size()).append("</h2>");
            
            sb.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            sb.append("<tr style='background: #2c3e50; color: white;'>")
              .append("<th>ID</th><th>Fecha/Hora</th><th>Cliente</th><th>Email Cliente</th>")
              .append("<th>Empleado</th><th>Email Empleado</th><th>Servicio</th><th>Estado</th>")
              .append("<th>¿Futura?</th><th>Días diferencia</th></tr>");
            
            for (Cita cita : todasCitas) {
                boolean esFutura = cita.getFechaHora().isAfter(LocalDateTime.now());
                long diasDiferencia = java.time.Duration.between(LocalDateTime.now(), cita.getFechaHora()).toDays();
                
                String colorFila = esFutura ? "#e8f5e8" : "#f5f5f5";
                
                sb.append("<tr style='background: ").append(colorFila).append(";'>")
                  .append("<td>").append(cita.getId()).append("</td>")
                  .append("<td>").append(cita.getFechaHora()).append("</td>")
                  .append("<td>").append(cita.getCliente() != null ? cita.getCliente().getNombre() : "null").append("</td>")
                  .append("<td>").append(cita.getCliente() != null ? cita.getCliente().getEmail() : "null").append("</td>")
                  .append("<td>").append(cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : "null").append("</td>")
                  .append("<td>").append(cita.getEmpleado() != null ? cita.getEmpleado().getEmail() : "null").append("</td>")
                  .append("<td>").append(cita.getServicio() != null ? cita.getServicio().getNombre() : "null").append("</td>")
                  .append("<td>").append(cita.getEstado()).append("</td>")
                  .append("<td>").append(esFutura ? "✅ SÍ" : "❌ NO").append("</td>")
                  .append("<td>").append(diasDiferencia).append(" días</td>")
                  .append("</tr>");
            }
            
            sb.append("</table>");
            
            // Estadísticas por empleado
            sb.append("<h2>📊 Estadísticas por Empleado</h2>");
            List<Empleado> empleados = empleadoService.listarActivos();
            
            for (Empleado emp : empleados) {
                List<Cita> citasEmp = citaService.listarPorEmpleado(emp);
                long futuras = citasEmp.stream().filter(c -> c.getFechaHora().isAfter(LocalDateTime.now())).count();
                
                sb.append("<h3>").append(emp.getNombre()).append(" (").append(emp.getEmail()).append(")</h3>");
                sb.append("<p><strong>Total citas:</strong> ").append(citasEmp.size())
                  .append(" | <strong>Citas futuras:</strong> ").append(futuras)
                  .append(" | <strong>Citas pasadas:</strong> ").append(citasEmp.size() - futuras).append("</p>");
            }
            
        } catch (Exception e) {
            sb.append("<p style='color:red'>Error: ").append(e.getMessage()).append("</p>");
            e.printStackTrace();
        }
        
        return sb.toString();
    }
}   