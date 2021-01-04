package com.kwgdev.msscspringstatemachine.services;

import com.kwgdev.msscspringstatemachine.domain.Payment;
import com.kwgdev.msscspringstatemachine.domain.PaymentEvent;
import com.kwgdev.msscspringstatemachine.domain.PaymentState;
import com.kwgdev.msscspringstatemachine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * created by kw on 1/4/2021 @ 7:39 AM
 */
@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("State should be NEW");
        System.out.println(savedPayment.getState());

        // save PRE_AUTH state to database
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        // get payment by paymentId back from database and verify state change to PRE_AUTH
        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        // in Test results we are expected a state of PRE_AUTH
        System.out.println("State should be PRE_AUTH");
        System.out.println(sm.getState().toString());

        // if we were doing a real test we would do some assertions first
        System.out.println(preAuthedPayment);
    }
}