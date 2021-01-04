package com.kwgdev.msscspringstatemachine.repository;

import com.kwgdev.msscspringstatemachine.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * created by kw on 1/4/2021 @ 6:38 AM
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
