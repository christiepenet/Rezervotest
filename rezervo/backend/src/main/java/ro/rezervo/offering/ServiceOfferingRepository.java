package ro.rezervo.offering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    @Query("select s from ServiceOffering s join fetch s.provider where s.active = true order by s.name")
    List<ServiceOffering> findAllActive();

    @Query("select s from ServiceOffering s join fetch s.provider where s.active = true and s.provider.id = :providerId order by s.name")
    List<ServiceOffering> findActiveByProvider(@Param("providerId") Long providerId);

    @Query("select s from ServiceOffering s join fetch s.provider where s.id = :id and s.active = true")
    Optional<ServiceOffering> findActiveById(@Param("id") Long id);
}
