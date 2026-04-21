package com.clinica.service;

import com.clinica.dto.RegisterPacienteRequest;
import com.clinica.entity.Paciente;
import com.clinica.entity.Usuario;
import com.clinica.enums.Rol;
import com.clinica.repository.PacienteRepository;
import com.clinica.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PacienteRepository pacienteRepository,
                       PasswordEncoder passwordEncoder,
                       EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional
    public void registrarPaciente(RegisterPacienteRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está en uso");
        }

        try {
            Usuario usuario = new Usuario();
            usuario.setUsername(request.getUsername());
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            usuario.setCorreo(request.getCorreo());
            usuario.setTelefono(request.getTelefono());
            usuario.setNombre(request.getNombre());
            usuario.setApellido1(request.getApellido1());
            usuario.setApellido2(request.getApellido2());
            usuario.setRol(Rol.PACIENTE);
            usuario = usuarioRepository.saveAndFlush(usuario);

            Paciente paciente = new Paciente();
            paciente.setUsuario(usuario);
            paciente.setFechaNacimiento(request.getFechaNacimiento());
            paciente.setSexo(request.getSexo());
            paciente.setDireccion(request.getDireccion());
            pacienteRepository.saveAndFlush(paciente);
            entityManager.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Otro usuario tomó ese username o correo antes de confirmar. Intenta con otro.");
        }
    }
}
