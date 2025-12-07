package com.barberapp.dto;

import java.time.LocalDateTime;

public class CitaRequest {
    private Long clienteId;
    private Long empleadoId;
    private Long servicioId;
    private LocalDateTime fechaHora;
    private String notas;
    
    // Constructor
    public CitaRequest() {}
    
    // Getters y Setters
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public Long getEmpleadoId() {
        return empleadoId;
    }
    
    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }
    
    public Long getServicioId() {
        return servicioId;
    }
    
    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }
} 