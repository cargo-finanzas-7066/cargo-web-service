package com.mitocode.repository;

import com.mitocode.entity.PaymentScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentScheduleRepository extends JpaRepository<PaymentScheduleEntity, Long> {
    List<PaymentScheduleEntity> findBySimulationIdOrderByPeriod(Integer simulationId);
    void deleteBySimulationId(Integer simulationId);
}
