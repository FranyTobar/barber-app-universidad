package com.barberapp.service.impl;

import com.barberapp.entity.Empleado;
import com.barberapp.entity.Rol;
import com.barberapp.entity.Usuario;
import com.barberapp.repository.EmpleadoRepository;
import com.barberapp.repository.UsuarioRepository;
import com.barberapp.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public Empleado crearEmpleado(Empleado empleado) {
        System.out.println("📝 Creando empleado: " + empleado.getNombre() + " - Email: " + empleado.getEmail());
        Empleado guardado = empleadoRepository.save(empleado);
        System.out.println("✅ Empleado creado con ID: " + guardado.getId());
        return guardado;
    }
    
    @Override
    public Optional<Empleado> buscarPorId(Long id) {
        System.out.println("🔍 Buscando empleado por ID: " + id);
        Optional<Empleado> empleado = empleadoRepository.findById(id);
        if (empleado.isPresent()) {
            System.out.println("✅ Empleado encontrado: " + empleado.get().getNombre() + 
                             " - Email: " + empleado.get().getEmail() +
                             " - Especialidad: " + empleado.get().getEspecialidad());
        } else {
            System.out.println("❌ Empleado no encontrado para ID: " + id);
        }
        return empleado;
    }
    
    @Override
    public Optional<Empleado> buscarPorEmail(String email) {
        System.out.println("🔍 Buscando empleado por email: " + email);
        
        // Primero buscar como Usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuario encontrado: " + usuario.getNombre() + 
                             " - ID: " + usuario.getId() + 
                             " - Rol: " + usuario.getRol());
            
            // Verificar si es BARBERO
            if (usuario.getRol() == Rol.BARBERO) {
                // Buscar como Empleado usando el ID del usuario
                Optional<Empleado> empleadoOpt = empleadoRepository.findById(usuario.getId());
                
                if (empleadoOpt.isPresent()) {
                    Empleado empleado = empleadoOpt.get();
                    System.out.println("✅ Empleado encontrado: " + empleado.getNombre() + 
                                     " - Especialidad: " + empleado.getEspecialidad());
                    return empleadoOpt;
                } else {
                    System.out.println("⚠️  Usuario es BARBERO pero no se encontró en tabla empleados");
                    
                    // Crear automáticamente un empleado a partir del usuario
                    Empleado nuevoEmpleado = new Empleado();
                    nuevoEmpleado.setId(usuario.getId());
                    nuevoEmpleado.setNombre(usuario.getNombre());
                    nuevoEmpleado.setEmail(usuario.getEmail());
                    nuevoEmpleado.setPassword(usuario.getPassword());
                    nuevoEmpleado.setTelefono(usuario.getTelefono());
                    nuevoEmpleado.setRol(Rol.BARBERO);
                    nuevoEmpleado.setEspecialidad("CORTE");
                    nuevoEmpleado.setHorarioTrabajo("COMPLETO");
                    
                    Empleado guardado = empleadoRepository.save(nuevoEmpleado);
                    System.out.println("✅ Empleado creado automáticamente: " + guardado.getNombre());
                    return Optional.of(guardado);
                }
            } else {
                System.out.println("❌ Usuario encontrado pero no es BARBERO (Rol: " + usuario.getRol() + ")");
            }
        } else {
            System.out.println("❌ Usuario no encontrado para email: " + email);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Empleado> listarTodos() {
        List<Empleado> empleados = empleadoRepository.findAll();
        System.out.println("📋 Total empleados en BD: " + empleados.size());
        return empleados;
    }
    
    @Override
    public List<Empleado> listarActivos() {
        // Listar todos los empleados (no tenemos campo activo)
        List<Empleado> empleados = empleadoRepository.findAll();
        System.out.println("📋 Empleados disponibles: " + empleados.size());
        
        System.out.println("=== LISTA COMPLETA DE BARBEROS ===");
        for (Empleado emp : empleados) {
            System.out.println("   👨‍💼 ID: " + emp.getId() + 
                             " | Nombre: " + emp.getNombre() + 
                             " | Email: " + emp.getEmail() +
                             " | Especialidad: " + emp.getEspecialidad() +
                             " | Rol: " + (emp.getRol() != null ? emp.getRol() : "Sin rol"));
        }
        System.out.println("===================================");
        
        return empleados;
    }
    
    @Override
    public List<Empleado> buscarPorEspecialidad(String especialidad) {
        System.out.println("🔍 Buscando empleados por especialidad: " + especialidad);
        List<Empleado> empleados = empleadoRepository.findByEspecialidad(especialidad);
        System.out.println("   Encontrados: " + empleados.size());
        return empleados;
    }
    
    @Override
    public List<Empleado> buscarPorHorarioTrabajo(String horario) {
        System.out.println("🔍 Buscando empleados por horario: " + horario);
        // Implementación simple - filtrar en memoria
        List<Empleado> todos = empleadoRepository.findAll();
        List<Empleado> filtrados = todos.stream()
            .filter(e -> e.getHorarioTrabajo() != null && 
                        e.getHorarioTrabajo().toLowerCase().contains(horario.toLowerCase()))
            .toList();
        System.out.println("   Encontrados: " + filtrados.size());
        return filtrados;
    }
    
    @Override
    public Empleado actualizarEmpleado(Long id, Empleado empleadoActualizado) {
        System.out.println("🔄 Actualizando empleado ID: " + id);
        return empleadoRepository.findById(id)
            .map(empleadoExistente -> {
                if (empleadoActualizado.getNombre() != null) {
                    empleadoExistente.setNombre(empleadoActualizado.getNombre());
                }
                if (empleadoActualizado.getEmail() != null) {
                    empleadoExistente.setEmail(empleadoActualizado.getEmail());
                }
                if (empleadoActualizado.getTelefono() != null) {
                    empleadoExistente.setTelefono(empleadoActualizado.getTelefono());
                }
                if (empleadoActualizado.getEspecialidad() != null) {
                    empleadoExistente.setEspecialidad(empleadoActualizado.getEspecialidad());
                }
                if (empleadoActualizado.getHorarioTrabajo() != null) {
                    empleadoExistente.setHorarioTrabajo(empleadoActualizado.getHorarioTrabajo());
                }
                if (empleadoActualizado.getCalificacionPromedio() != null) {
                    empleadoExistente.setCalificacionPromedio(empleadoActualizado.getCalificacionPromedio());
                }
                
                Empleado actualizado = empleadoRepository.save(empleadoExistente);
                System.out.println("✅ Empleado actualizado: " + actualizado.getNombre());
                return actualizado;
            })
            .orElseThrow(() -> {
                System.out.println("❌ Empleado no encontrado con ID: " + id);
                return new RuntimeException("Empleado no encontrado con ID: " + id);
            });
    }
    
    @Override
    public void desactivarEmpleado(Long id) {
        System.out.println("⏸️  Desactivando empleado ID: " + id);
        empleadoRepository.findById(id).ifPresent(empleado -> {
            empleado.setActivo(false);
            empleadoRepository.save(empleado);
            System.out.println("✅ Empleado desactivado: " + empleado.getNombre());
        });
    }
    
    @Override
    public void activarEmpleado(Long id) {
        System.out.println("▶️  Activando empleado ID: " + id);
        empleadoRepository.findById(id).ifPresent(empleado -> {
            empleado.setActivo(true);
            empleadoRepository.save(empleado);
            System.out.println("✅ Empleado activado: " + empleado.getNombre());
        });
    }
    
    @Override
    public List<Empleado> buscarPorNombre(String nombre) {
        System.out.println("🔍 Buscando empleados por nombre: " + nombre);
        List<Empleado> todos = empleadoRepository.findAll();
        List<Empleado> filtrados = todos.stream()
            .filter(e -> e.getNombre() != null && 
                        e.getNombre().toLowerCase().contains(nombre.toLowerCase()))
            .toList();
        System.out.println("   Encontrados: " + filtrados.size());
        return filtrados;
    }
    
    @Override
    public long contarEmpleadosActivos() {
        long count = empleadoRepository.count();
        System.out.println("📊 Total empleados activos: " + count);
        return count;
    }
    
    @Override
    public List<Empleado> listarDisponibles() {
        System.out.println("📋 Listando empleados disponibles");
        List<Empleado> empleados = listarActivos();
        System.out.println("   Empleados disponibles: " + empleados.size());
        return empleados;
    }
    
    // Método helper para diagnóstico
    public void mostrarTodosEmpleados() {
        System.out.println("=== 🔍 DIAGNÓSTICO COMPLETO DE EMPLEADOS ===");
        List<Empleado> empleados = empleadoRepository.findAll();
        System.out.println("Total empleados en BD: " + empleados.size());
        
        for (Empleado emp : empleados) {
            System.out.println("   ID: " + emp.getId() + 
                             " | Nombre: " + emp.getNombre() +
                             " | Email: " + emp.getEmail() +
                             " | Especialidad: " + emp.getEspecialidad() +
                             " | Rol: " + (emp.getRol() != null ? emp.getRol() : "N/A") +
                             " | Activo: " + emp.isActivo());
        }
        
        // También mostrar usuarios con rol BARBERO
        System.out.println("\n=== 🔍 USUARIOS CON ROL BARBERO ===");
        List<Usuario> usuarios = usuarioRepository.findAll();
        int barberosCount = 0;
        for (Usuario user : usuarios) {
            if (user.getRol() == Rol.BARBERO) {
                barberosCount++;
                System.out.println("   ID: " + user.getId() + 
                                 " | Nombre: " + user.getNombre() +
                                 " | Email: " + user.getEmail() +
                                 " | Rol: " + user.getRol());
            }
        }
        System.out.println("Total usuarios BARBERO: " + barberosCount);
        System.out.println("============================================");
    }
}