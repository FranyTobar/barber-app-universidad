package com.barberapp.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario {
    
    @Column(name = "preferencias_corte")
    private String preferenciasCorte;
    
    @Column(name = "frecuencia_visita")
    private String frecuenciaVisita; // "SEMANAL", "QUINCENAL", "MENSUAL"
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();
    
    // Constructor
    public Cliente() {
        super();
        this.setRol(Rol.CLIENTE);
    }
    
    public Cliente(String email, String password, String nombre) {
        super(email, password, nombre, Rol.CLIENTE);
    }
    
    // Getters y Setters específicos
    public String getPreferenciasCorte() {
        return preferenciasCorte;
    }
    
    public void setPreferenciasCorte(String preferenciasCorte) {
        this.preferenciasCorte = preferenciasCorte;
    }
    
    public String getFrecuenciaVisita() {
        return frecuenciaVisita;
    }
    
    public void setFrecuenciaVisita(String frecuenciaVisita) {
        this.frecuenciaVisita = frecuenciaVisita;
    }
    
    public List<Cita> getCitas() {
        return citas;
    }
    
    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
    
    // Método para agregar cita (COMENTADO temporalmente para evitar errores)
    
    public void agregarCita(Cita cita) {
        citas.add(cita);
        cita.setCliente(this);
    }
    
} 