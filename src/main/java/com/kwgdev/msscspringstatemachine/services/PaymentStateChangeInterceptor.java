package com.kwgdev.msscspringstatemachine.services;

import com.kwgdev.msscspringstatemachine.domain.Payment;
import com.kwgdev.msscspringstatemachine.domain.PaymentEvent;
import com.kwgdev.msscspringstatemachine.domain.PaymentState;
import com.kwgdev.msscspringstatemachine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * created by kw on 1/4/2021 @ 7:28 AM
 */
@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine) {
        // before a State change, we are going to say **IF** a message is present,
        // we'll get the paymentId off the header and pass that
        // so if we find a paymentId Header then we will go to the paymentRepository and get the payment (by paymentId)
        // then we will set the State from the state being passed in to the method and then save it to the repository
        //
        // and this is how we will persist state changes to the database
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                    .ifPresent(paymentId -> {
                        Payment payment = paymentRepository.getOne(paymentId);
                        payment.setState(state.getId());
                        paymentRepository.save(payment);
                    });
        });
    }
}
