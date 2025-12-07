package com.barberapp.service.impl;

import com.barberapp.entity.Servicio;
import com.barberapp.repository.ServicioRepository;
import com.barberapp.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {
    
    @Autowired
    private ServicioRepository servicioRepository;
    
    @Override
    public Servicio crearServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }
    
    @Override
    public Optional<Servicio> buscarPorId(Long id) {
        return servicioRepository.findById(id);
    }
    
    @Override
    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }
    
    @Override
    public List<Servicio> listarActivos() {
        return servicioRepository.findByActivoTrue();
    }
    
    @Override
    public List<Servicio> buscarPorNombre(String nombre) {
        return servicioRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    @Override
    public List<Servicio> buscarPorCategoria(String categoria) {
        return servicioRepository.findByCategoria(categoria);
    }
    
    @Override
    public List<Servicio> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return servicioRepository.findByPrecioBetween(
            precioMin.doubleValue(), 
            precioMax.doubleValue()
        );
    }
    
    @Override
    public Servicio actualizarServicio(Long id, Servicio servicioActualizado) {
        return servicioRepository.findById(id).map(servicio -> {
            servicio.setNombre(servicioActualizado.getNombre());
            servicio.setDescripcion(servicioActualizado.getDescripcion());
            servicio.setPrecio(servicioActualizado.getPrecio());
            servicio.setDuracionMinutos(servicioActualizado.getDuracionMinutos());
            servicio.setCategoria(servicioActualizado.getCategoria());
            return servicioRepository.save(servicio);
        }).orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
    }
    
    @Override
    public void desactivarServicio(Long id) {
        servicioRepository.findById(id).ifPresent(servicio -> {
            servicio.setActivo(false);
            servicioRepository.save(servicio);
        });
    }
    
    @Override
    public void activarServicio(Long id) {
        servicioRepository.findById(id).ifPresent(servicio -> {
            servicio.setActivo(true);
            servicioRepository.save(servicio);
        });
    }
    
    @Override
    public List<Servicio> listarPorPrecioAscendente() {
        return servicioRepository.findByActivoTrueOrderByPrecioAsc();
    }
    
    @Override
    public List<Servicio> listarPorPrecioDescendente() {
        return servicioRepository.findByActivoTrueOrderByPrecioDesc();
    }
    
    @Override
    public long contarServiciosActivos() {
        return servicioRepository.countByActivoTrue();
    }
} 