package com.ejemplo.usuarios.service;

import com.ejemplo.usuarios.dto.UpdateUserRequest;
import com.ejemplo.usuarios.dto.UserResponse;
import com.ejemplo.usuarios.entity.EstadoUsuario;
import com.ejemplo.usuarios.entity.Usuario;
import com.ejemplo.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UserResponse> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<UserResponse> obtenerPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toResponse);
    }

    public Optional<UserResponse> actualizar(Long id, UpdateUserRequest request) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOptional.get();
        String nuevoEmail = request.getEmail().trim().toLowerCase();

        if (!usuario.getEmail().equalsIgnoreCase(nuevoEmail)
                && usuarioRepository.existsByEmail(nuevoEmail)) {
            throw new IllegalArgumentException("El email ya está registrado por otro usuario");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(nuevoEmail);

        Usuario actualizado = usuarioRepository.save(usuario);
        return Optional.of(toResponse(actualizado));
    }

    public boolean desactivar(Long id) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        return true;
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
