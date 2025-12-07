package com.barberapp.repository;

import com.barberapp.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    
    // Buscar por especialidad
    List<Empleado> findByEspecialidad(String especialidad);
    
    // Si necesitas buscar por email (ajustar según tu entidad)
    // Optional<Empleado> findByEmail(String email);
    
    // Si tienes campo activo
    // List<Empleado> findByActivoTrue();
} 