package com.transaction.training.repository;

import com.transaction.training.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByAccountId(Long accountId);
    
    List<Order> findByStatus(Order.OrderStatus status);
}
