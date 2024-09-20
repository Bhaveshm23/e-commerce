package com.express.order_picking_service.repository;

import com.express.order_picking_service.model.Picking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PickingRepository extends JpaRepository<Picking, Long> {
   Optional<Picking> findByOrderNumber(String orderNumber);
    Optional<Picking> findById(Long pickingId);

}
