package com.barberapp.repository;

import com.barberapp.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    
    // Buscar servicios activos
    List<Servicio> findByActivoTrue();
    
    // Buscar por nombre
    List<Servicio> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por categoría
    List<Servicio> findByCategoria(String categoria);
    
    // Buscar por rango de precio
    List<Servicio> findByPrecioBetween(Double precioMin, Double precioMax);
    
    // Ordenar por precio ascendente
    List<Servicio> findByActivoTrueOrderByPrecioAsc();
    
    // Ordenar por precio descendente
    List<Servicio> findByActivoTrueOrderByPrecioDesc();
    
    // Servicios más populares (con más citas)
    @Query("SELECT s FROM Servicio s WHERE s.activo = true ORDER BY " +
           "(SELECT COUNT(c) FROM Cita c WHERE c.servicio = s) DESC")
    List<Servicio> findServiciosMasPopulares();
    
    // Contar servicios activos
    long countByActivoTrue();
} 