package com.barberapp.service;

import com.barberapp.entity.Empleado;
import java.util.List;
import java.util.Optional;

public interface EmpleadoService {
    
    Empleado crearEmpleado(Empleado empleado);
    
    Optional<Empleado> buscarPorId(Long id);
    
    Optional<Empleado> buscarPorEmail(String email);
    
    List<Empleado> listarTodos();
    
    List<Empleado> listarActivos();
    
    List<Empleado> buscarPorEspecialidad(String especialidad);
    
    List<Empleado> buscarPorHorarioTrabajo(String horario);
    
    Empleado actualizarEmpleado(Long id, Empleado empleadoActualizado);
    
    void desactivarEmpleado(Long id);
    
    void activarEmpleado(Long id);
    
    List<Empleado> buscarPorNombre(String nombre);
    
    long contarEmpleadosActivos();
    
    List<Empleado> listarDisponibles();
} 