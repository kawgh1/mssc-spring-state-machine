package com.kwgdev.msscspringstatemachine.services;

import com.kwgdev.msscspringstatemachine.domain.Payment;
import com.kwgdev.msscspringstatemachine.domain.PaymentEvent;
import com.kwgdev.msscspringstatemachine.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

/**
 * created by kw on 1/4/2021 @ 7:00 AM
 */
public interface PaymentService {

    Payment newPayment(Payment payment);

    // what these will do is they'll take in a paymentId, an return a State based on the paymentId
    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
