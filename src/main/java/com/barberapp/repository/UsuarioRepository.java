package com.barberapp.repository;

import com.barberapp.entity.Usuario;
import com.barberapp.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);
    
    // Buscar usuarios por rol
    List<Usuario> findByRol(Rol rol);
    
    // Verificar si existe un usuario con un email
    boolean existsByEmail(String email);
    
    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();
    
    // Buscar por nombre (ignorando mayúsculas/minúsculas)
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
} 