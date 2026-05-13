package org.eduardomaravill.mopt.gpsconsumer.repository;

import org.eduardomaravill.mopt.gpsconsumer.models.GpsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGpsRepository extends JpaRepository<GpsEventEntity,Long> {
}
