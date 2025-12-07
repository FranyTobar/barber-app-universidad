package com.barberapp.repository;

import com.barberapp.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Buscar cliente por email (heredado de Usuario)
    Optional<Cliente> findByEmail(String email);
    
    // Buscar clientes activos
    List<Cliente> findByActivoTrue();
    
    // Buscar clientes por nombre
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    
    // Contar clientes activos
    long countByActivoTrue();
    
    // Buscar clientes por frecuencia de visita
    List<Cliente> findByFrecuenciaVisita(String frecuenciaVisita);
    
    // Query personalizada: Clientes con más citas
    @Query("SELECT c FROM Cliente c WHERE SIZE(c.citas) > 0 ORDER BY SIZE(c.citas) DESC")
    List<Cliente> findClientesConMasCitas();
} 