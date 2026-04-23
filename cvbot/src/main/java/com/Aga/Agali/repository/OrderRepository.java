package com.Aga.Agali.repository;

import com.Aga.Agali.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByStripeSessionId(String stripeSessionId);
    Optional<Order> findTopByUserOrderByCreatedAtDesc(com.Aga.Agali.entity.User user);
}