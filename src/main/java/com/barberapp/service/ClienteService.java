package com.barberapp.service;

import com.barberapp.entity.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    
    Cliente crearCliente(Cliente cliente);
    
    Optional<Cliente> buscarPorId(Long id);
    
    Optional<Cliente> buscarPorEmail(String email);
    
    List<Cliente> listarTodos();
    
    List<Cliente> listarActivos();
    
    Cliente actualizarCliente(Long id, Cliente clienteActualizado);
    
    void desactivarCliente(Long id);
    
    void activarCliente(Long id);
    
    List<Cliente> buscarPorNombre(String nombre);
    
    long contarClientesActivos();
    
    List<Cliente> buscarPorFrecuenciaVisita(String frecuencia);
} 