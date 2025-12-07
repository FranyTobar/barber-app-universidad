package com.barberapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empleados")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Empleado extends Usuario {
    
    @Column(name = "especialidad")
    private String especialidad; // "CORTE", "BARBA", "AMBOS"
    
    @Column(name = "calificacion_promedio")
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;
    
    @Column(name = "horario_trabajo")
    private String horarioTrabajo; // "MATUTINO", "VESPERTINO", "COMPLETO"
    
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();
    
    // Constructor
    public Empleado() {
        super();
        this.setRol(Rol.BARBERO);
    }
    
    public Empleado(String email, String password, String nombre, String especialidad) {
        super(email, password, nombre, Rol.BARBERO);
        this.especialidad = especialidad;
    }
    
    // Getters y Setters específicos
    public String getEspecialidad() {
        return especialidad;
    }
    
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    public BigDecimal getCalificacionPromedio() {
        return calificacionPromedio;
    }
    
    public void setCalificacionPromedio(BigDecimal calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
    
    public String getHorarioTrabajo() {
        return horarioTrabajo;
    }
    
    public void setHorarioTrabajo(String horarioTrabajo) {
        this.horarioTrabajo = horarioTrabajo;
    }
    
    public List<Cita> getCitas() {
        return citas;
    }
    
    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
    
    // Método para agregar cita (COMENTADO temporalmente)
    /*
    public void agregarCita(Cita cita) {
        citas.add(cita);
        cita.setEmpleado(this);
    }
    */
} 