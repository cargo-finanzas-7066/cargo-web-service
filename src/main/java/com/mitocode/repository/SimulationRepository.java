package com.mitocode.repository;

import com.mitocode.entity.SimulationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SimulationRepository extends JpaRepository<SimulationEntity, Integer> {
    Page<SimulationEntity> findByOwnerIdAndArchivedFalse(Integer ownerId, Pageable pageable);
    Page<SimulationEntity> findByArchivedFalse(Pageable pageable);
    Optional<SimulationEntity> findByIdAndOwnerIdAndArchivedFalse(Integer id, Integer ownerId);
    Optional<SimulationEntity> findByIdAndArchivedFalse(Integer id);
    long countByOwnerIdAndArchivedFalse(Integer ownerId);
    long countByArchivedFalse();
    Page<SimulationEntity> findByOwnerIdAndArchivedFalseOrderByCreatedAtTimestampDesc(Integer ownerId, Pageable pageable);
    Page<SimulationEntity> findByArchivedFalseOrderByCreatedAtTimestampDesc(Pageable pageable);
}
