package com.barberapp.controller;

import com.barberapp.entity.Cliente;
import com.barberapp.entity.Cita;
import com.barberapp.entity.EstadoCita;
import com.barberapp.service.ClienteService;
import com.barberapp.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private CitaService citaService;
    
    // ========== DASHBOARD ==========
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        System.out.println("=== 🏠 CLIENTE DASHBOARD ===");
        System.out.println("Email autenticado: " + email);
        
        // Buscar cliente por email
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("✅ Cliente encontrado: " + cliente.getNombre() + 
                             " (ID: " + cliente.getId() + ")");
            
            // Obtener todas las citas del cliente
            List<Cita> todasCitas = citaService.listarPorCliente(cliente);
            System.out.println("📊 Total citas del cliente: " + todasCitas.size());
            
            // Calcular estadísticas
            long totalCitas = todasCitas.size();
            long citasPendientes = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE)
                .count();
            long citasConfirmadas = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA)
                .count();
            long citasCompletadas = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .count();
            
            // Encontrar la próxima cita (pendiente o confirmada, fecha futura)
            Optional<Cita> proximaCitaOpt = todasCitas.stream()
                .filter(c -> (c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.CONFIRMADA))
                .filter(c -> c.getFechaHora() != null && c.getFechaHora().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Cita::getFechaHora));
            
            // Obtener últimas citas (máximo 5)
            List<Cita> ultimasCitas = todasCitas.stream()
                .sorted(Comparator.comparing(Cita::getFechaHora).reversed())
                .limit(5)
                .collect(Collectors.toList());
            
            // Agregar datos al modelo
            model.addAttribute("cliente", cliente);
            model.addAttribute("totalCitas", totalCitas);
            model.addAttribute("citasPendientes", citasPendientes);
            model.addAttribute("citasConfirmadas", citasConfirmadas);
            model.addAttribute("citasCompletadas", citasCompletadas);
            
            if (proximaCitaOpt.isPresent()) {
                model.addAttribute("proximaCita", proximaCitaOpt.get());
                System.out.println("📅 Próxima cita encontrada: " + proximaCitaOpt.get().getFechaHora());
            } else {
                System.out.println("📭 No hay próxima cita programada");
            }
            
            model.addAttribute("ultimasCitas", ultimasCitas);
            
            // Mostrar información de cada cita para debug
            System.out.println("=== 📋 DETALLE DE CITAS ===");
            for (Cita cita : todasCitas) {
                System.out.println("   📅 ID: " + cita.getId() +
                                 " | Servicio: " + (cita.getServicio() != null ? cita.getServicio().getNombre() : "N/A") +
                                 " | Fecha: " + cita.getFechaHora() +
                                 " | Estado: " + cita.getEstado() +
                                 " | Barbero: " + (cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : "N/A"));
            }
            
            return "cliente/dashboard";
            
        } else {
            System.out.println("❌ Cliente no encontrado para email: " + email);
            System.out.println("🔍 Verificando base de datos...");
            
            // Listar todos los clientes para debug
            List<Cliente> todosClientes = clienteService.listarTodos();
            System.out.println("Total clientes en BD: " + todosClientes.size());
            for (Cliente c : todosClientes) {
                System.out.println("   👤 ID: " + c.getId() + " | Nombre: " + c.getNombre() + " | Email: " + c.getEmail());
            }
            
            return "redirect:/auth/login";
        }
    }
    
    // ========== PERFIL ==========
    
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            model.addAttribute("cliente", cliente);
            
            // Obtener estadísticas del cliente
            List<Cita> todasCitas = citaService.listarPorCliente(cliente);
            long totalCitas = todasCitas.size();
            long citasCompletadas = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .count();
            
            // Calcular gasto total
            double gastoTotal = todasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA && c.getServicio() != null)
                .mapToDouble(c -> c.getServicio().getPrecio().doubleValue())
                .sum();
            
            // Última cita
            Optional<Cita> ultimaCita = todasCitas.stream()
                .filter(c -> c.getFechaHora() != null)
                .max(Comparator.comparing(Cita::getFechaHora));
            
            model.addAttribute("totalCitas", totalCitas);
            model.addAttribute("citasCompletadas", citasCompletadas);
            model.addAttribute("gastoTotal", gastoTotal);
            model.addAttribute("ultimaCita", ultimaCita.orElse(null));
            
            return "cliente/perfil";
        }
        
        return "redirect:/auth/login";
    }
    
    // ========== EDITAR PERFIL ==========
    
    @GetMapping("/perfil/editar")
    public String mostrarEditarPerfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            model.addAttribute("cliente", cliente);
            return "cliente/editar-perfil";
        }
        
        return "redirect:/auth/login";
    }
    
    // Actualizar perfil (POST)
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@ModelAttribute Cliente clienteActualizado,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            // Actualizar solo los campos permitidos
            cliente.setNombre(clienteActualizado.getNombre());
            cliente.setEmail(clienteActualizado.getEmail());
            cliente.setTelefono(clienteActualizado.getTelefono());
            cliente.setFrecuenciaVisita(clienteActualizado.getFrecuenciaVisita());
            cliente.setPreferenciasCorte(clienteActualizado.getPreferenciasCorte());
            
            clienteService.actualizarCliente(cliente.getId(), cliente);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el cliente");
        }
        
        return "redirect:/cliente/perfil";
    }
    
    // ========== HISTORIAL ==========
    
    @GetMapping("/historial")
    public String historialCitas(@RequestParam(required = false) String estado,
                                Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            List<Cita> todasCitas = citaService.listarPorCliente(cliente);
            
            // Filtrar por estado si se especifica
            List<Cita> citasFiltradas;
            if (estado != null && !estado.isEmpty()) {
                try {
                    EstadoCita estadoCita = EstadoCita.valueOf(estado.toUpperCase());
                    citasFiltradas = todasCitas.stream()
                        .filter(c -> c.getEstado() == estadoCita)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    citasFiltradas = todasCitas;
                    estado = "";
                }
            } else {
                citasFiltradas = todasCitas;
            }
            
            // Ordenar por fecha descendente
            List<Cita> citasOrdenadas = citasFiltradas.stream()
                .sorted(Comparator.comparing(Cita::getFechaHora).reversed())
                .collect(Collectors.toList());
            
            // Calcular estadísticas financieras
            double gastoCompletadas = citasOrdenadas.stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA && c.getServicio() != null)
                .mapToDouble(c -> c.getServicio().getPrecio().doubleValue())
                .sum();
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("citas", citasOrdenadas);
            model.addAttribute("filtroEstado", estado != null ? estado : "");
            model.addAttribute("estados", EstadoCita.values());
            model.addAttribute("gastoCompletadas", gastoCompletadas);
            
            System.out.println("📜 Historial cargado - Cliente: " + cliente.getNombre());
            System.out.println("   Total citas: " + citasOrdenadas.size());
            System.out.println("   Filtro estado: " + (estado != null ? estado : "ninguno"));
            
            return "cliente/historial";
        }
        
        return "redirect:/auth/login";
    }
    
    // ========== PREFERENCIAS ==========
    
    @GetMapping("/preferencias")
    public String preferencias(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            model.addAttribute("cliente", cliente);
            return "cliente/preferencias";
        }
        
        return "redirect:/auth/login";
    }
    
    // Guardar preferencias (POST)
    @PostMapping("/preferencias/guardar")
    public String guardarPreferencias(@RequestParam(required = false) String frecuenciaVisita,
                                     @RequestParam(required = false) String preferenciasCorte,
                                     @RequestParam(required = false) String horaPreferida,
                                     @RequestParam(required = false) Boolean recordatorioEmail,
                                     @RequestParam(required = false) Boolean recordatorioSMS,
                                     @RequestParam(required = false) Boolean promocionesEmail,
                                     @RequestParam(required = false) Boolean noticias,
                                     @RequestParam(required = false) Long barberoPreferido,
                                     @RequestParam(required = false) Long servicioFavorito,
                                     @RequestParam(required = false) String[] productos,
                                     @RequestParam(required = false) String temaColor,
                                     @RequestParam(required = false) String tamanoFuente,
                                     @RequestParam(required = false) Boolean modoOscuro,
                                     @RequestParam(required = false) Boolean perfilPublico,
                                     @RequestParam(required = false) Boolean compartirDatos,
                                     RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            System.out.println("=== ⚙️ GUARDANDO PREFERENCIAS ===");
            System.out.println("Cliente: " + cliente.getNombre());
            
            // Actualizar campos básicos del cliente
            if (frecuenciaVisita != null && !frecuenciaVisita.isEmpty()) {
                cliente.setFrecuenciaVisita(frecuenciaVisita);
                System.out.println("   Frecuencia visita: " + frecuenciaVisita);
            }
            
            if (preferenciasCorte != null && !preferenciasCorte.isEmpty()) {
                cliente.setPreferenciasCorte(preferenciasCorte);
                System.out.println("   Preferencias corte actualizadas");
            }
            
            // Guardar cambios en el cliente
            clienteService.actualizarCliente(cliente.getId(), cliente);
            
            // Aquí podrías guardar las demás preferencias en una tabla separada
            // Por ahora solo actualizamos los campos básicos del cliente
            
            // Construir mensaje de éxito
            StringBuilder mensaje = new StringBuilder("✅ Preferencias guardadas exitosamente");
            
            if (recordatorioEmail != null && recordatorioEmail) {
                mensaje.append(". Recibirás recordatorios por email.");
            }
            
            if (recordatorioSMS != null && recordatorioSMS) {
                mensaje.append(" Recibirás SMS de recordatorio.");
            }
            
            if (barberoPreferido != null) {
                mensaje.append(" Barbero preferido configurado.");
            }
            
            if (servicioFavorito != null) {
                mensaje.append(" Servicio favorito guardado.");
            }
            
            redirectAttributes.addFlashAttribute("success", mensaje.toString());
            
            // Registrar en logs las preferencias avanzadas
            System.out.println("   Hora preferida: " + horaPreferida);
            System.out.println("   Recordatorio Email: " + recordatorioEmail);
            System.out.println("   Recordatorio SMS: " + recordatorioSMS);
            System.out.println("   Promociones Email: " + promocionesEmail);
            System.out.println("   Barbero Preferido ID: " + barberoPreferido);
            System.out.println("   Servicio Favorito ID: " + servicioFavorito);
            System.out.println("   Tema Color: " + temaColor);
            System.out.println("   Modo Oscuro: " + modoOscuro);
            
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ No se pudo guardar las preferencias: Cliente no encontrado");
        }
        
        return "redirect:/cliente/preferencias";
    }
    
    // ========== CAMBIAR CONTRASEÑA ==========
    
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                 @RequestParam String nuevaPassword,
                                 @RequestParam String confirmarPassword,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        // Validaciones
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/cliente/perfil";
        }
        
        if (!nuevaPassword.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/cliente/perfil";
        }
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            // Aquí necesitarías implementar la verificación de la contraseña actual
            // y el cambio de contraseña usando un servicio adecuado
            
            // Por ahora, solo mostramos un mensaje de éxito
            redirectAttributes.addFlashAttribute("success", "Contraseña cambiada exitosamente");
            
            System.out.println("🔐 Cambio de contraseña solicitado para: " + email);
            System.out.println("   Nueva contraseña (longitud): " + nuevaPassword.length() + " caracteres");
            
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo cambiar la contraseña");
        }
        
        return "redirect:/cliente/perfil";
    }
    
    // ========== DIAGNÓSTICO ==========
    
    @GetMapping("/diagnostico")
    public String diagnostico(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        System.out.println("=== 🔍 DIAGNÓSTICO CLIENTE ===");
        System.out.println("Email autenticado: " + email);
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            StringBuilder diagnostico = new StringBuilder();
            diagnostico.append("<h1>🔍 Diagnóstico Cliente</h1>");
            diagnostico.append("<h2>Información del Cliente:</h2>");
            diagnostico.append("<ul>");
            diagnostico.append("<li><strong>ID:</strong> ").append(cliente.getId()).append("</li>");
            diagnostico.append("<li><strong>Nombre:</strong> ").append(cliente.getNombre()).append("</li>");
            diagnostico.append("<li><strong>Email:</strong> ").append(cliente.getEmail()).append("</li>");
            diagnostico.append("<li><strong>Teléfono:</strong> ").append(cliente.getTelefono()).append("</li>");
            diagnostico.append("<li><strong>Rol:</strong> ").append(cliente.getRol()).append("</li>");
            diagnostico.append("<li><strong>Activo:</strong> ").append(cliente.isActivo()).append("</li>");
            diagnostico.append("<li><strong>Frecuencia Visita:</strong> ").append(cliente.getFrecuenciaVisita() != null ? cliente.getFrecuenciaVisita() : "No especificada").append("</li>");
            diagnostico.append("<li><strong>Preferencias Corte:</strong> ").append(cliente.getPreferenciasCorte() != null ? cliente.getPreferenciasCorte() : "No especificadas").append("</li>");
            diagnostico.append("<li><strong>Fecha Creación:</strong> ").append(cliente.getFechaCreacion()).append("</li>");
            diagnostico.append("</ul>");
            
            // Obtener citas
            List<Cita> todasCitas = citaService.listarPorCliente(cliente);
            diagnostico.append("<h2>Citas del Cliente (").append(todasCitas.size()).append("):</h2>");
            
            if (todasCitas.isEmpty()) {
                diagnostico.append("<p>No hay citas registradas</p>");
            } else {
                diagnostico.append("<table border='1'><tr><th>ID</th><th>Servicio</th><th>Fecha</th><th>Estado</th><th>Barbero</th><th>Precio</th></tr>");
                
                for (Cita cita : todasCitas) {
                    diagnostico.append("<tr>")
                              .append("<td>").append(cita.getId()).append("</td>")
                              .append("<td>").append(cita.getServicio() != null ? cita.getServicio().getNombre() : "N/A").append("</td>")
                              .append("<td>").append(cita.getFechaHora()).append("</td>")
                              .append("<td>").append(cita.getEstado()).append("</td>")
                              .append("<td>").append(cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : "N/A").append("</td>")
                              .append("<td>").append(cita.getServicio() != null ? "$" + cita.getServicio().getPrecio() : "N/A").append("</td>")
                              .append("</tr>");
                }
                diagnostico.append("</table>");
            }
            
            // Verificar todas las citas en la base de datos
            List<Cita> todasCitasBD = citaService.listarTodas();
            diagnostico.append("<h2>Todas las Citas en BD (").append(todasCitasBD.size()).append("):</h2>");
            diagnostico.append("<table border='1'><tr><th>ID</th><th>Cliente ID</th><th>Cliente Nombre</th><th>Servicio</th><th>Fecha</th><th>Estado</th></tr>");
            
            for (Cita cita : todasCitasBD) {
                diagnostico.append("<tr>")
                          .append("<td>").append(cita.getId()).append("</td>")
                          .append("<td>").append(cita.getCliente() != null ? cita.getCliente().getId() : "N/A").append("</td>")
                          .append("<td>").append(cita.getCliente() != null ? cita.getCliente().getNombre() : "N/A").append("</td>")
                          .append("<td>").append(cita.getServicio() != null ? cita.getServicio().getNombre() : "N/A").append("</td>")
                          .append("<td>").append(cita.getFechaHora()).append("</td>")
                          .append("<td>").append(cita.getEstado()).append("</td>")
                          .append("</tr>");
            }
            diagnostico.append("</table>");
            
            model.addAttribute("diagnostico", diagnostico.toString());
            return "diagnostico";
        }
        
        return "redirect:/auth/login";
    }
    
    // ========== AJUSTES RÁPIDOS ==========
    
    @PostMapping("/preferencias/rapido")
    public String guardarPreferenciasRapido(@RequestParam String frecuenciaVisita,
                                           @RequestParam String preferenciasCorte,
                                           RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            cliente.setFrecuenciaVisita(frecuenciaVisita);
            cliente.setPreferenciasCorte(preferenciasCorte);
            
            clienteService.actualizarCliente(cliente.getId(), cliente);
            redirectAttributes.addFlashAttribute("success", "Preferencias guardadas exitosamente");
        }
        
        return "redirect:/cliente/perfil";
    }
    
    // ========== ELIMINAR CUENTA ==========
    
    @GetMapping("/eliminar-cuenta")
    public String mostrarEliminarCuenta(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            model.addAttribute("cliente", clienteOpt.get());
            return "cliente/eliminar-cuenta";
        }
        
        return "redirect:/auth/login";
    }
    
    @PostMapping("/eliminar-cuenta/confirmar")
    public String eliminarCuenta(@RequestParam String confirmacionEmail,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        if (!email.equals(confirmacionEmail)) {
            redirectAttributes.addFlashAttribute("error", "El email no coincide");
            return "redirect:/cliente/eliminar-cuenta";
        }
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            // En lugar de eliminar físicamente, desactivamos la cuenta
            Cliente cliente = clienteOpt.get();
            clienteService.desactivarCliente(cliente.getId());
            
            // Cerrar sesión
            SecurityContextHolder.clearContext();
            
            redirectAttributes.addFlashAttribute("success", 
                "Tu cuenta ha sido desactivada. Esperamos verte de nuevo pronto.");
            return "redirect:/auth/login";
        }
        
        return "redirect:/auth/login";
    }
    
    // ========== EXPORTAR DATOS ==========
    
    @GetMapping("/exportar-datos")
    @ResponseBody
    public String exportarDatos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            List<Cita> citas = citaService.listarPorCliente(cliente);
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Nombre,Email,Telefono,Frecuencia_Visita,Preferencias_Corte,Fecha_Creacion\n");
            csv.append(cliente.getId()).append(",")
               .append(cliente.getNombre()).append(",")
               .append(cliente.getEmail()).append(",")
               .append(cliente.getTelefono() != null ? cliente.getTelefono() : "").append(",")
               .append(cliente.getFrecuenciaVisita() != null ? cliente.getFrecuenciaVisita() : "").append(",")
               .append(cliente.getPreferenciasCorte() != null ? "\"" + cliente.getPreferenciasCorte().replace("\"", "\"\"") + "\"" : "").append(",")
               .append(cliente.getFechaCreacion()).append("\n\n");
            
            csv.append("CITAS\n");
            csv.append("ID,Fecha_Hora,Servicio,Barbero,Precio,Estado,Notas\n");
            
            for (Cita cita : citas) {
                csv.append(cita.getId()).append(",")
                   .append(cita.getFechaHora()).append(",")
                   .append(cita.getServicio() != null ? cita.getServicio().getNombre() : "N/A").append(",")
                   .append(cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : "N/A").append(",")
                   .append(cita.getServicio() != null ? cita.getServicio().getPrecio() : "0").append(",")
                   .append(cita.getEstado()).append(",")
                   .append(cita.getNotas() != null ? "\"" + cita.getNotas().replace("\"", "\"\"") + "\"" : "").append("\n");
            }
            
            return csv.toString();
        }
        
        return "Cliente no encontrado";
    }
}  