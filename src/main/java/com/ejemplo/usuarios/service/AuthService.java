package com.ejemplo.usuarios.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ejemplo.usuarios.dto.LoginRequest;
import com.ejemplo.usuarios.dto.RegisterRequest;
import com.ejemplo.usuarios.dto.UserResponse;
import com.ejemplo.usuarios.entity.EstadoUsuario;
import com.ejemplo.usuarios.entity.Usuario;
import com.ejemplo.usuarios.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registrar(RegisterRequest request) {
        if (request == null
                || request.getNombre() == null
                || request.getEmail() == null
                || request.getPassword() == null) {
            throw new IllegalArgumentException("Datos de registro incompletos");
        }

        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = new Usuario(
                request.getNombre().trim(),
                emailNormalizado,
                passwordEncoder.encode(request.getPassword()),
                EstadoUsuario.ACTIVO
        );

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    public Optional<UserResponse> login(LoginRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        return usuarioRepository.findByEmail(emailNormalizado)
                .filter(usuario -> usuario.getEstado() == EstadoUsuario.ACTIVO)
                .filter(usuario -> passwordEncoder.matches(request.getPassword(), usuario.getPassword()))
                .map(this::toResponse);
    }

    private UserResponse toResponse(Usuario usuario) {
        return new UserResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getEstado()
        );
    }
}
