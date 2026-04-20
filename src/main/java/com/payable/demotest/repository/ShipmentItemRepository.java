package com.payable.demotest.repository;

import com.payable.demotest.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, String> {
    List<ShipmentItem> findByShipmentShipmentId(String shipmentId);
}

