package com.barberapp.service;

import com.barberapp.entity.Cita;
import com.barberapp.entity.Cliente;
import com.barberapp.entity.Empleado;
import com.barberapp.entity.EstadoCita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaService {
    
    Cita crearCita(Cita cita);
    
    Optional<Cita> buscarPorId(Long id);
    
    List<Cita> listarTodas();
    
    List<Cita> listarPorCliente(Cliente cliente);
    
    List<Cita> listarPorEmpleado(Empleado empleado);
    
    List<Cita> listarPorEstado(EstadoCita estado);
    
    List<Cita> listarEntreFechas(LocalDateTime inicio, LocalDateTime fin);
    
    Cita actualizarCita(Long id, Cita citaActualizada);
    
    void cancelarCita(Long id, String motivo);
    
    void completarCita(Long id);
    
    void confirmarCita(Long id);
    
    boolean verificarDisponibilidad(Empleado empleado, LocalDateTime fechaHora, Integer duracionMinutos);
    
    List<Cita> listarCitasHoy();
    
    List<Cita> listarCitasSemana();
    
    List<Cita> listarCitasClienteHoy(Cliente cliente);
    
    List<Cita> listarCitasEmpleadoHoy(Empleado empleado);
    
    long contarCitasPorEstado(EstadoCita estado);
} 