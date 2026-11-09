package ro.rezervo.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Programarile active ale unui furnizor care se suprapun cu intervalul [from, to). */
    @Query("""
            select a from Appointment a
            where a.service.provider.id = :providerId
              and a.status = :status
              and a.startTime < :to
              and a.endTime > :from
            """)
    List<Appointment> findOverlapping(@Param("providerId") Long providerId,
                                      @Param("status") AppointmentStatus status,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);

    @Query("""
            select a from Appointment a
            join fetch a.service s
            join fetch s.provider
            join fetch a.client
            where a.client.id = :clientId
            order by a.startTime desc
            """)
    List<Appointment> findForClient(@Param("clientId") Long clientId);

    @Query("""
            select a from Appointment a
            join fetch a.service s
            join fetch s.provider
            join fetch a.client
            where s.provider.id = :providerId
            order by a.startTime desc
            """)
    List<Appointment> findForProvider(@Param("providerId") Long providerId);

    @Query("""
            select a from Appointment a
            join fetch a.service s
            join fetch s.provider
            join fetch a.client
            where a.id = :id
            """)
    Optional<Appointment> findDetailedById(@Param("id") Long id);

    long countByClient_IdAndStatusAndStartTimeAfter(Long clientId, AppointmentStatus status, LocalDateTime after);
}
