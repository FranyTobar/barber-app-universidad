package com.barberapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;
    
    @Column(length = 500)
    private String notas;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    // Constructor
    public Cita() {
    }
    
    public Cita(Cliente cliente, Empleado empleado, Servicio servicio, LocalDateTime fechaHora) {
        this.cliente = cliente;
        this.empleado = empleado;
        this.servicio = servicio;
        this.fechaHora = fechaHora;
        // CORRECCIÓN: Verificar que servicio no sea null
        if (servicio != null && servicio.getDuracionMinutos() != null) {
            this.fechaHoraFin = fechaHora.plusMinutes(servicio.getDuracionMinutos());
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Empleado getEmpleado() {
        return empleado;
    }
    
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
    
    public Servicio getServicio() {
        return servicio;
    }
    
    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
        // CORRECCIÓN: Verificar que servicio y fechaHora no sean null
        if (this.fechaHora != null && servicio != null && servicio.getDuracionMinutos() != null) {
            this.fechaHoraFin = this.fechaHora.plusMinutes(servicio.getDuracionMinutos());
        }
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
        // CORRECCIÓN: Verificar que servicio no sea null
        if (this.servicio != null && this.servicio.getDuracionMinutos() != null) {
            this.fechaHoraFin = fechaHora.plusMinutes(this.servicio.getDuracionMinutos());
        }
    }
    
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
    
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
    
    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }
    
    public EstadoCita getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    // Método auxiliar para calcular duración
    public Integer getDuracionMinutos() {
        if (servicio != null && servicio.getDuracionMinutos() != null) {
            return servicio.getDuracionMinutos();
        }
        return 0; // Duración por defecto
    }
    
    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", cliente=" + (cliente != null ? cliente.getNombre() : "null") +
                ", empleado=" + (empleado != null ? empleado.getNombre() : "null") +
                ", servicio=" + (servicio != null ? servicio.getNombre() : "null") +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                '}';
    }
}