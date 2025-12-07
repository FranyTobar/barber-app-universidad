package com.barberapp.repository;

import com.barberapp.entity.Cita;
import com.barberapp.entity.Cliente;
import com.barberapp.entity.Empleado;
import com.barberapp.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Buscar citas por cliente
    List<Cita> findByCliente(Cliente cliente);
    
    // Buscar citas por empleado
    List<Cita> findByEmpleado(Empleado empleado);
    
    // Buscar citas por estado
    List<Cita> findByEstado(EstadoCita estado);
    
    // Buscar citas entre fechas
    List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Buscar citas por cliente y estado
    List<Cita> findByClienteAndEstado(Cliente cliente, EstadoCita estado);
    
    // Buscar citas por empleado y estado
    List<Cita> findByEmpleadoAndEstado(Empleado empleado, EstadoCita estado);
    
    // Buscar citas por fecha específica (CORREGIDO PARA H2)
    // ❌ ANTES: FUNCTION('DATE', c.fechaHora) = :fecha
    // ✅ AHORA: CAST(c.fechaHora AS date) = :fecha
    @Query("SELECT c FROM Cita c WHERE CAST(c.fechaHora AS date) = :fecha")
    List<Cita> findByFecha(@Param("fecha") LocalDate fecha);
    
    // Citas pendientes para un empleado en una fecha
    List<Cita> findByEmpleadoAndFechaHoraBetweenAndEstado(
            Empleado empleado, 
            LocalDateTime inicio, 
            LocalDateTime fin, 
            EstadoCita estado
    );
    
    // Verificar si hay conflicto de horario
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE " +
           "c.empleado = :empleado AND " +
           "c.estado NOT IN ('CANCELADA', 'NO_ASISTIO') AND " +
           "((c.fechaHora <= :fin AND c.fechaHoraFin >= :inicio))")
    boolean existsConflictoHorario(
            @Param("empleado") Empleado empleado, 
            @Param("inicio") LocalDateTime inicio, 
            @Param("fin") LocalDateTime fin
    );
    
    // Citas del día actual (CORREGIDO PARA H2)
    // ❌ ANTES: FUNCTION('DATE', c.fechaHora) = CURRENT_DATE
    // ✅ AHORA: CAST(c.fechaHora AS date) = CURRENT_DATE
    @Query("SELECT c FROM Cita c WHERE CAST(c.fechaHora AS date) = CURRENT_DATE ORDER BY c.fechaHora")
    List<Cita> findCitasHoy();
    
    // Citas de la semana actual (CORREGIDO)
    @Query("SELECT c FROM Cita c WHERE c.fechaHora >= :inicioSemana AND c.fechaHora < :finSemana ORDER BY c.fechaHora")
    List<Cita> findCitasEstaSemana(@Param("inicioSemana") LocalDateTime inicioSemana, 
                                   @Param("finSemana") LocalDateTime finSemana);
    
    // Estadísticas de citas por mes (CORREGIDO PARA H2)
    // H2 usa EXTRACT(MONTH FROM ...) en lugar de MONTH()
    @Query("SELECT EXTRACT(MONTH FROM c.fechaHora) as mes, COUNT(c) as total FROM Cita c WHERE EXTRACT(YEAR FROM c.fechaHora) = :year GROUP BY EXTRACT(MONTH FROM c.fechaHora)")
    List<Object[]> findEstadisticasCitasPorMes(@Param("year") int year);
    
    // Métodos adicionales útiles
    List<Cita> findByClienteId(Long clienteId);
    
    @Query("SELECT c FROM Cita c WHERE c.empleado = :empleado AND c.estado = :estado ORDER BY c.fechaHora")
    List<Cita> findByEmpleadoAndEstadoOrderByFechaHora(@Param("empleado") Empleado empleado,
                                                      @Param("estado") EstadoCita estado);
    
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.estado = :estado")
    long countByEstado(@Param("estado") EstadoCita estado);
}  