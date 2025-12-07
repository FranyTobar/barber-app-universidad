package com.barberapp.service;

import com.barberapp.entity.Usuario;
import java.util.Optional;

public interface UsuarioService {
    
    // Solo métodos esenciales que no necesitan PasswordEncoder
    Optional<Usuario> buscarPorEmail(String email);
    boolean existeEmail(String email);
} 