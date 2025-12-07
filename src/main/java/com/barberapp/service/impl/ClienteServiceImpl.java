package com.barberapp.service.impl;

import com.barberapp.entity.Cliente;
import com.barberapp.repository.ClienteRepository;
import com.barberapp.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Cliente crearCliente(Cliente cliente) {
        // Encriptar contraseÃ±a
        
        return clienteRepository.save(cliente);
    }
    
    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }
    
    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
    
    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
    
    @Override
    public List<Cliente> listarActivos() {
        return clienteRepository.findByActivoTrue();
    }
    
    @Override
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNombre(clienteActualizado.getNombre());
            cliente.setTelefono(clienteActualizado.getTelefono());
            cliente.setPreferenciasCorte(clienteActualizado.getPreferenciasCorte());
            cliente.setFrecuenciaVisita(clienteActualizado.getFrecuenciaVisita());
            
            if (clienteActualizado.getPassword() != null && !clienteActualizado.getPassword().isEmpty()) {
                cliente.setPassword(passwordEncoder.encode(clienteActualizado.getPassword()));
            }
            
            return clienteRepository.save(cliente);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }
    
    @Override
    public void desactivarCliente(Long id) {
        clienteRepository.findById(id).ifPresent(cliente -> {
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        });
    }
    
    @Override
    public void activarCliente(Long id) {
        clienteRepository.findById(id).ifPresent(cliente -> {
            cliente.setActivo(true);
            clienteRepository.save(cliente);
        });
    }
    
    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    @Override
    public long contarClientesActivos() {
        return clienteRepository.countByActivoTrue();
    }
    
    @Override
    public List<Cliente> buscarPorFrecuenciaVisita(String frecuencia) {
        return clienteRepository.findByFrecuenciaVisita(frecuencia);
    }
} 
