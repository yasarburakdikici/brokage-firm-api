package com.brokage.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brokage.challenge.entity.Order;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdAndCreateDateBetween(String customerId, Instant startDate, Instant endDate);
}


