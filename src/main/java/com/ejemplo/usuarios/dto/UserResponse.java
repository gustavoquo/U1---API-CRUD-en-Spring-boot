package com.ejemplo.usuarios.dto;

import com.ejemplo.usuarios.entity.EstadoUsuario;

public class UserResponse {
    private Long id;
    private String nombre;
    private String email;
    private EstadoUsuario estado;

    public UserResponse(Long id, String nombre, String email, EstadoUsuario estado) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }
}
