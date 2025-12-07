package com.barberapp.service.impl;

import com.barberapp.entity.*;
import com.barberapp.repository.CitaRepository;
import com.barberapp.service.CitaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {
    
    @Autowired
    private CitaRepository citaRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Cita crearCita(Cita cita) {
        System.out.println("=== 🎯 CitaServiceImpl.crearCita() INICIADO ===");
        System.out.println("📝 Datos recibidos:");
        System.out.println("   Cliente: " + (cita.getCliente() != null ? 
            cita.getCliente().getNombre() + " (ID: " + cita.getCliente().getId() + ")" : "null"));
        System.out.println("   Empleado: " + (cita.getEmpleado() != null ? 
            cita.getEmpleado().getNombre() + " (ID: " + cita.getEmpleado().getId() + ")" : "null"));
        System.out.println("   Servicio: " + (cita.getServicio() != null ? 
            cita.getServicio().getNombre() + " (ID: " + cita.getServicio().getId() + ")" : "null"));
        System.out.println("   Fecha/Hora: " + cita.getFechaHora());
        System.out.println("   Estado: " + cita.getEstado());
        System.out.println("   Notas: " + cita.getNotas());
        
        // 🔧 PASO 1: LIMPIAR CACHÉ ANTES DE GUARDAR
        System.out.println("🧹 Paso 1: Limpiando caché de Hibernate...");
        entityManager.clear();
        
        // Validar que todos los datos estén presentes
        if (cita.getCliente() == null) {
            System.out.println("❌ ERROR: Cliente es null");
            throw new RuntimeException("Cliente no especificado");
        }
        if (cita.getEmpleado() == null) {
            System.out.println("❌ ERROR: Empleado es null");
            throw new RuntimeException("Empleado no especificado");
        }
        if (cita.getServicio() == null) {
            System.out.println("❌ ERROR: Servicio es null");
            throw new RuntimeException("Servicio no especificado");
        }
        if (cita.getFechaHora() == null) {
            System.out.println("❌ ERROR: Fecha/Hora es null");
            throw new RuntimeException("Fecha/Hora no especificada");
        }
        
        // Validar disponibilidad antes de crear
        Integer duracion = cita.getServicio() != null ? cita.getServicio().getDuracionMinutos() : 30;
        System.out.println("🔍 Verificando disponibilidad...");
        System.out.println("   Duración: " + duracion + " minutos");
        
        boolean disponible = verificarDisponibilidad(cita.getEmpleado(), cita.getFechaHora(), duracion);
        System.out.println("   Disponible: " + disponible);
        
        if (!disponible) {
            System.out.println("❌ ERROR: Empleado no disponible en ese horario");
            throw new RuntimeException("El empleado no está disponible en ese horario");
        }
        
        // Calcular hora de fin
        if (cita.getServicio() != null && cita.getFechaHora() != null) {
            LocalDateTime horaFin = cita.getFechaHora().plusMinutes(duracion);
            cita.setFechaHoraFin(horaFin);
            System.out.println("   Fecha fin calculada: " + horaFin + " (+" + duracion + " minutos)");
        }
        
        // Asegurar estado
        if (cita.getEstado() == null) {
            cita.setEstado(EstadoCita.PENDIENTE);
            System.out.println("   Estado establecido: PENDIENTE (por defecto)");
        }
        
        // Asegurar fecha de creación
        if (cita.getFechaCreacion() == null) {
            cita.setFechaCreacion(LocalDateTime.now());
            System.out.println("   Fecha creación: " + cita.getFechaCreacion());
        }
        
        try {
            System.out.println("💾 Paso 2: Guardando cita en BD...");
            Cita citaGuardada = citaRepository.saveAndFlush(cita);
            
            System.out.println("🔄 Paso 3: Forzando flush y refresh...");
            entityManager.flush();
            entityManager.refresh(citaGuardada);
            
            System.out.println("✅ CITA GUARDADA EXITOSAMENTE");
            System.out.println("   🆔 ID asignado: " + citaGuardada.getId());
            
            // 🔍 PASO 4: VERIFICAR INMEDIATAMENTE QUE SE GUARDÓ
            System.out.println("🔍 Paso 4: Verificando persistencia...");
            Long totalCitas = citaRepository.count();
            System.out.println("   📊 Total citas en BD ahora: " + totalCitas);
            
            // Consulta directa a la BD
            List<Cita> todasCitas = citaRepository.findAll();
            System.out.println("   🔍 findAll() retorna: " + todasCitas.size() + " citas");
            
            // Verificar que nuestra cita está en la lista
            boolean encontrada = todasCitas.stream()
                .anyMatch(c -> c.getId().equals(citaGuardada.getId()));
            System.out.println("   ✅ Nuestra cita encontrada en findAll(): " + encontrada);
            
            // Verificar específicamente por cliente
            List<Cita> citasCliente = citaRepository.findByCliente(cita.getCliente());
            System.out.println("   👤 Citas del cliente " + cita.getCliente().getId() + ": " + citasCliente.size());
            
            return citaGuardada;
            
        } catch (Exception e) {
            System.out.println("❌ ERROR al guardar cita: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar cita: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<Cita> buscarPorId(Long id) {
        System.out.println("🔍 Buscando cita por ID: " + id);
        return citaRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarTodas() {
        System.out.println("=== 🔍 CitaServiceImpl.listarTodas() INICIADO ===");
        
        // 🔧 LIMPIAR CACHÉ ANTES DE CONSULTAR
        entityManager.clear();
        System.out.println("🧹 Caché limpiada antes de findAll()");
        
        List<Cita> citas = citaRepository.findAll();
        System.out.println("📊 Total citas encontradas: " + citas.size());
        
        // Debug: mostrar todas las citas
        for (int i = 0; i < Math.min(citas.size(), 10); i++) { // Mostrar solo las primeras 10
            Cita c = citas.get(i);
            System.out.println("   📅 [" + (i+1) + "] ID: " + c.getId() + 
                             " | Cliente: " + (c.getCliente() != null ? c.getCliente().getId() : "null") +
                             " | Fecha: " + c.getFechaHora() +
                             " | Estado: " + c.getEstado());
        }
        
        if (citas.size() > 10) {
            System.out.println("   ... y " + (citas.size() - 10) + " más");
        }
        
        return citas;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarPorCliente(Cliente cliente) {
        System.out.println("=== 👤 CitaServiceImpl.listarPorCliente() INICIADO ===");
        System.out.println("   Cliente: " + cliente.getNombre() + " (ID: " + cliente.getId() + ")");
        
        // 🔧 LIMPIAR CACHÉ
        entityManager.clear();
        
        List<Cita> citas = citaRepository.findByCliente(cliente);
        System.out.println("📊 Citas encontradas: " + citas.size());
        
        for (Cita c : citas) {
            System.out.println("   📅 ID: " + c.getId() + 
                             " | Servicio: " + (c.getServicio() != null ? c.getServicio().getNombre() : "null") +
                             " | Fecha: " + c.getFechaHora() +
                             " | Estado: " + c.getEstado());
        }
        
        return citas;
    }
    
    @Override
    public List<Cita> listarPorEmpleado(Empleado empleado) {
        return citaRepository.findByEmpleado(empleado);
    }
    
    @Override
    public List<Cita> listarPorEstado(EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }
    
    @Override
    public List<Cita> listarEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.findByFechaHoraBetween(inicio, fin);
    }
    
    @Override
    public Cita actualizarCita(Long id, Cita citaActualizada) {
        return citaRepository.findById(id).map(cita -> {
            // Si cambia el empleado o la fecha, validar disponibilidad
            if (!cita.getEmpleado().equals(citaActualizada.getEmpleado()) || 
                !cita.getFechaHora().equals(citaActualizada.getFechaHora())) {
                
                Integer duracion = citaActualizada.getServicio() != null ? 
                    citaActualizada.getServicio().getDuracionMinutos() : 30;
                
                if (!verificarDisponibilidad(citaActualizada.getEmpleado(), 
                    citaActualizada.getFechaHora(), duracion)) {
                    throw new RuntimeException("El empleado no está disponible en ese horario");
                }
            }
            
            cita.setEmpleado(citaActualizada.getEmpleado());
            cita.setServicio(citaActualizada.getServicio());
            cita.setFechaHora(citaActualizada.getFechaHora());
            cita.setNotas(citaActualizada.getNotas());
            cita.setEstado(citaActualizada.getEstado());
            
            // Recalcular hora de fin
            if (cita.getServicio() != null && cita.getFechaHora() != null) {
                LocalDateTime horaFin = cita.getFechaHora().plusMinutes(
                    cita.getServicio().getDuracionMinutos()
                );
                cita.setFechaHoraFin(horaFin);
            }
            
            return citaRepository.save(cita);
        }).orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }
    
    @Override
    public void cancelarCita(Long id, String motivo) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setEstado(EstadoCita.CANCELADA);
            if (motivo != null && !motivo.isEmpty()) {
                cita.setNotas((cita.getNotas() != null ? cita.getNotas() + "\n" : "") + 
                             "Cancelación: " + motivo);
            }
            citaRepository.save(cita);
        });
    }
    
    @Override
    public void completarCita(Long id) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setEstado(EstadoCita.COMPLETADA);
            citaRepository.save(cita);
        });
    }
    
    @Override
    public void confirmarCita(Long id) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setEstado(EstadoCita.CONFIRMADA);
            citaRepository.save(cita);
        });
    }
    
    @Override
    public boolean verificarDisponibilidad(Empleado empleado, LocalDateTime fechaHora, Integer duracionMinutos) {
        if (empleado == null || fechaHora == null || duracionMinutos == null) {
            return false;
        }
        
        LocalDateTime inicio = fechaHora;
        LocalDateTime fin = fechaHora.plusMinutes(duracionMinutos);
        
        // Verificar si hay conflicto
        return !citaRepository.existsConflictoHorario(empleado, inicio, fin);
    }
    
    @Override
    public List<Cita> listarCitasHoy() {
        System.out.println("🔧 listarCitasHoy - solución compatible con H2 Database");
        
        List<Cita> todasCitas = citaRepository.findAll();
        
        LocalDate hoy = LocalDate.now();
        List<Cita> citasHoy = todasCitas.stream()
            .filter(cita -> {
                if (cita.getFechaHora() == null) return false;
                LocalDate fechaCita = cita.getFechaHora().toLocalDate();
                return fechaCita.equals(hoy);
            })
            .toList();
        
        System.out.println("✅ Total citas hoy: " + citasHoy.size() + " de " + todasCitas.size() + " totales");
        return citasHoy;
    }
    
    @Override
    public List<Cita> listarCitasSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY).plusDays(1);
        
        LocalDateTime inicio = inicioSemana.atStartOfDay();
        LocalDateTime fin = finSemana.atStartOfDay();
        
        return citaRepository.findCitasEstaSemana(inicio, fin);
    }
    
    @Override
    public List<Cita> listarCitasClienteHoy(Cliente cliente) {
        System.out.println("🔧 listarCitasClienteHoy para: " + cliente.getNombre());
        
        List<Cita> todasCitasCliente = citaRepository.findByCliente(cliente);
        
        LocalDate hoy = LocalDate.now();
        List<Cita> citasHoyCliente = todasCitasCliente.stream()
            .filter(cita -> {
                if (cita.getFechaHora() == null) return false;
                LocalDate fechaCita = cita.getFechaHora().toLocalDate();
                return fechaCita.equals(hoy);
            })
            .toList();
        
        System.out.println("✅ Citas hoy para cliente " + cliente.getNombre() + ": " + citasHoyCliente.size());
        return citasHoyCliente;
    }
    
    @Override
    public List<Cita> listarCitasEmpleadoHoy(Empleado empleado) {
        System.out.println("🔧 listarCitasEmpleadoHoy para: " + empleado.getNombre());
        
        List<Cita> todasCitasEmpleado = citaRepository.findByEmpleado(empleado);
        
        LocalDate hoy = LocalDate.now();
        List<Cita> citasHoyEmpleado = todasCitasEmpleado.stream()
            .filter(cita -> {
                if (cita.getFechaHora() == null) return false;
                LocalDate fechaCita = cita.getFechaHora().toLocalDate();
                return fechaCita.equals(hoy);
            })
            .toList();
        
        System.out.println("✅ Citas hoy para " + empleado.getNombre() + ": " + citasHoyEmpleado.size() + " de " + todasCitasEmpleado.size() + " totales");
        return citasHoyEmpleado;
    }
    
    @Override
    public long contarCitasPorEstado(EstadoCita estado) {
        return citaRepository.findByEstado(estado).size();
    }
} 