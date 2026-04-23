package com.clinica.service;

import com.clinica.dto.CitaRequest;
import com.clinica.entity.BloqueHorario;
import com.clinica.entity.Cita;
import com.clinica.entity.Doctor;
import com.clinica.entity.Paciente;
import com.clinica.entity.Usuario;
import com.clinica.enums.EstadoCita;
import com.clinica.repository.BloqueHorarioRepository;
import com.clinica.repository.CitaRepository;
import com.clinica.repository.DoctorRepository;
import com.clinica.repository.PacienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaService {
    private final CitaRepository citaRepository;
    private final BloqueHorarioRepository bloqueHorarioRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;

    public CitaService(CitaRepository citaRepository,
                       BloqueHorarioRepository bloqueHorarioRepository,
                       PacienteRepository pacienteRepository,
                       DoctorRepository doctorRepository) {
        this.citaRepository = citaRepository;
        this.bloqueHorarioRepository = bloqueHorarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Cita> listarDoctor(Long usuarioId) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        return citaRepository.findByDoctor(doctor.getId());
    }

    public List<Cita> listarPaciente(Long usuarioId) {
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        return citaRepository.findByPaciente(paciente);
    }

    public List<Cita> listarDisponiblesSemana(LocalDate fechaBase) {
        LocalDate inicioSemanaDate = fechaBase.with(DayOfWeek.MONDAY);
        LocalDateTime inicio = inicioSemanaDate.atStartOfDay();
        LocalDateTime fin = inicio.plusDays(7);
        return citaRepository.findDisponiblesPorSemana(inicio, fin);
    }

    @Transactional
    public void crearCita(Usuario usuario, CitaRequest request) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        BloqueHorario bloque = bloqueHorarioRepository.findByIdForUpdate(request.getBloqueId())
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));

        LocalDateTime inicioCita = request.getInicioCita();
        LocalDateTime finCita = request.getFinCita();

        if (!bloque.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Solo puedes crear citas en tus propios bloques");
        }

        if (inicioCita == null || finCita == null || !inicioCita.isBefore(finCita)) {
            throw new IllegalArgumentException("La cita es inválida");
        }

        if (!inicioCita.toLocalDate().equals(bloque.getInicioBloque().toLocalDate())
                || !finCita.toLocalDate().equals(bloque.getInicioBloque().toLocalDate())) {
            throw new IllegalArgumentException("La cita debe tener la misma fecha del bloque seleccionado");
        }

        if (inicioCita.isBefore(bloque.getInicioBloque()) || finCita.isAfter(bloque.getFinBloque())) {
            throw new IllegalArgumentException("La cita debe quedar dentro del bloque reservado");
        }

        if (citaRepository.existeTraslapeEnBloque(bloque.getId(), inicioCita, finCita)) {
            throw new IllegalArgumentException("La cita choca con otra ya creada dentro de ese bloque");
        }

        Cita cita = new Cita();
        cita.setBloque(bloque);
        cita.setEstadoCita(EstadoCita.DISPONIBLE);
        cita.setInicioCita(inicioCita);
        cita.setFinCita(finCita);

        citaRepository.save(cita);
    }

    @Transactional
    public void reservarCita(Long citaId, Usuario usuario) {
        Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (cita.getEstadoCita() != EstadoCita.DISPONIBLE || cita.getPaciente() != null) {
            throw new IllegalArgumentException("La cita ya no está disponible");
        }

        cita.setPaciente(paciente);
        cita.setEstadoCita(EstadoCita.RESERVADA);
        citaRepository.save(cita);
    }

    @Transactional
    public void cancelarCitaDoctor(Long citaId, Usuario usuario) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!cita.getBloque().getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("No puedes modificar esa cita");
        }

        if (cita.getEstadoCita() == EstadoCita.ATENDIDA) {
            throw new IllegalArgumentException("No se puede cancelar una cita ya atendida");
        }

        cita.setPaciente(null);
        cita.setEstadoCita(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Transactional
    public void marcarAtendida(Long citaId, Usuario usuario) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!cita.getBloque().getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("No puedes modificar esa cita");
        }

        if (cita.getPaciente() == null || cita.getEstadoCita() != EstadoCita.RESERVADA) {
            throw new IllegalArgumentException("Solo se pueden atender citas reservadas con paciente");
        }

        cita.setEstadoCita(EstadoCita.ATENDIDA);
        citaRepository.save(cita);
    }

    @Transactional
    public void marcarAusente(Long citaId, Usuario usuario) {
        Doctor doctor = doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!cita.getBloque().getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("No puedes modificar esa cita");
        }

        if (cita.getPaciente() == null || cita.getEstadoCita() != EstadoCita.RESERVADA) {
            throw new IllegalArgumentException("Solo se puede marcar ausente una cita reservada");
        }

        cita.setEstadoCita(EstadoCita.AUSENTE);
        citaRepository.save(cita);
    }

    @Transactional
    public void cancelarCitaPaciente(Long citaId, Usuario usuario) {
        Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (cita.getPaciente() == null || !cita.getPaciente().getId().equals(paciente.getId())) {
            throw new IllegalArgumentException("Esa cita no te pertenece");
        }

        if (cita.getEstadoCita() != EstadoCita.RESERVADA) {
            throw new IllegalArgumentException("Solo puedes cancelar citas reservadas");
        }

        cita.setPaciente(null);
        cita.setEstadoCita(EstadoCita.DISPONIBLE);
        citaRepository.save(cita);
    }
}