package com.clinica.service;

import com.clinica.dto.BloqueRequest;
import com.clinica.entity.BloqueHorario;
import com.clinica.entity.Consultorio;
import com.clinica.entity.Doctor;
import com.clinica.entity.Usuario;
import com.clinica.enums.EstadoBloque;
import com.clinica.repository.BloqueHorarioRepository;
import com.clinica.repository.ConsultorioRepository;
import com.clinica.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BloqueHorarioService {
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultorioRepository consultorioRepository;

    public BloqueHorarioService(BloqueHorarioRepository bloqueHorarioRepository,
                                DoctorRepository doctorRepository,
                                ConsultorioRepository consultorioRepository) {
        this.bloqueHorarioRepository = bloqueHorarioRepository;
        this.doctorRepository = doctorRepository;
        this.consultorioRepository = consultorioRepository;
    }

    public List<BloqueHorario> listarPorDoctor(Long usuarioId) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        return bloqueHorarioRepository.findByDoctorOrderByInicioBloqueAsc(doctor);
    }

    @Transactional
    public void reservarBloque(Usuario usuario, BloqueRequest request) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        Consultorio consultorio = consultorioRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Debes crear el consultorio principal con ID 1"));

        LocalDateTime inicio = request.getInicioBloque();
        LocalDateTime fin = request.getFinBloque();

        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new IllegalArgumentException("El bloque es inválido");
        }

        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new IllegalArgumentException("El bloque debe iniciar y terminar el mismo día");
        }

        if (inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No puedes reservar bloques en el pasado");
        }

        if (bloqueHorarioRepository.existeConflicto(inicio, fin)) {
            throw new IllegalArgumentException("Ese horario ya fue reservado por otro doctor");
        }

        BloqueHorario bloque = new BloqueHorario();
        bloque.setDoctor(doctor);
        bloque.setConsultorio(consultorio);
        bloque.setInicioBloque(inicio);
        bloque.setFinBloque(fin);
        bloque.setEstadoBloque(EstadoBloque.RESERVADO);

        bloqueHorarioRepository.save(bloque);
    }
}