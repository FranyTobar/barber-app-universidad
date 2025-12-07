package com.barberapp.service;

import com.barberapp.entity.Usuario;
import com.barberapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;  // ← Usar repositorio directo
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== 🔍 BUSCANDO USUARIO ===");
        System.out.println("Email: " + email);
        
        // Usar repositorio directamente
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado: " + email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });
        
        System.out.println("✅ USUARIO ENCONTRADO:");
        System.out.println("   👤 ID: " + usuario.getId());
        System.out.println("   📧 Email: " + usuario.getEmail());
        System.out.println("   🏷️  Rol: " + usuario.getRol());
        System.out.println("   🔑 Password length: " + 
            (usuario.getPassword() != null ? usuario.getPassword().length() : "NULL"));
        System.out.println("   ✅ Activo: " + usuario.isActivo());
        
        String rol = usuario.getRol() != null ? usuario.getRol().name() : "CLIENTE";
        String authority = "ROLE_" + rol;
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authority));
        
        System.out.println("   🛡️  Authority: " + authority);
        
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
    }
} 