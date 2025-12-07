package com.barberapp.service;

import com.barberapp.entity.Servicio;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ServicioService {
    
    Servicio crearServicio(Servicio servicio);
    
    Optional<Servicio> buscarPorId(Long id);
    
    List<Servicio> listarTodos();
    
    List<Servicio> listarActivos();
    
    List<Servicio> buscarPorNombre(String nombre);
    
    List<Servicio> buscarPorCategoria(String categoria);
    
    List<Servicio> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);
    
    Servicio actualizarServicio(Long id, Servicio servicioActualizado);
    
    void desactivarServicio(Long id);
    
    void activarServicio(Long id);
    
    List<Servicio> listarPorPrecioAscendente();
    
    List<Servicio> listarPorPrecioDescendente();
    
    long contarServiciosActivos();
} 