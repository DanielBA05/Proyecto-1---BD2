package com.clinica.repository;

import com.clinica.entity.Cita;
import com.clinica.entity.Paciente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cita c where c.id = :id")
    Optional<Cita> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select case when count(c) > 0 then true else false end
        from Cita c
        where c.bloque.id = :bloqueId
          and c.estadoCita <> com.clinica.enums.EstadoCita.CANCELADA
          and c.inicioCita < :fin
          and c.finCita > :inicio
    """)
    boolean existeTraslapeEnBloque(@Param("bloqueId") Long bloqueId,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);

    @Query("""
        select c from Cita c
        join fetch c.bloque b
        join fetch b.doctor d
        join fetch d.usuario du
        where c.estadoCita = com.clinica.enums.EstadoCita.DISPONIBLE
          and c.inicioCita >= :inicioSemana
          and c.inicioCita < :finSemana
        order by c.inicioCita asc
    """)
    List<Cita> findDisponiblesPorSemana(@Param("inicioSemana") LocalDateTime inicioSemana,
                                        @Param("finSemana") LocalDateTime finSemana);

    @Query("""
        select c from Cita c
        join fetch c.bloque b
        join fetch b.doctor d
        join fetch d.usuario du
        left join fetch c.paciente p
        left join fetch p.usuario pu
        where d.id = :doctorId
        order by c.inicioCita desc, c.id desc
    """)
    List<Cita> findByDoctor(@Param("doctorId") Long doctorId);

    @Query("""
        select c from Cita c
        join fetch c.bloque b
        join fetch b.doctor d
        join fetch d.usuario du
        where c.paciente = :paciente
        order by c.inicioCita desc
    """)
    List<Cita> findByPaciente(@Param("paciente") Paciente paciente);

    boolean existsByPacienteIdAndEstadoCita(Long pacienteId, com.clinica.enums.EstadoCita estadoCita);

    void deleteByPacienteId(Long pacienteId);
}