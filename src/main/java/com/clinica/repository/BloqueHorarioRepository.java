package com.clinica.repository;

import com.clinica.entity.BloqueHorario;
import com.clinica.entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {
    @Query("""
        select case when count(b) > 0 then true else false end
        from BloqueHorario b
        where b.estadoBloque = com.clinica.enums.EstadoBloque.RESERVADO
          and b.inicioBloque < :fin
          and b.finBloque > :inicio
    """)
    boolean existeConflicto(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    List<BloqueHorario> findByDoctorOrderByInicioBloqueAsc(Doctor doctor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BloqueHorario b where b.id = :id")
    Optional<BloqueHorario> findByIdForUpdate(@Param("id") Long id);
}
