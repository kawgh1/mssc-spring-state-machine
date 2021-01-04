package com.kwgdev.msscspringstatemachine.services;

import com.kwgdev.msscspringstatemachine.domain.Payment;
import com.kwgdev.msscspringstatemachine.domain.PaymentEvent;
import com.kwgdev.msscspringstatemachine.domain.PaymentState;
import com.kwgdev.msscspringstatemachine.repository.PaymentRepository;
import com.kwgdev.msscspringstatemachine.services.PaymentStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * created by kw on 1/4/2021 @ 7:03 AM
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {

        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);

        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {


        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);

        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {


        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);

        return sm;
    }

    // base method for sending a message containing the paymentId
    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {

        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER,paymentId)
                .build();

        // want the State Machine to be aware of the payment ID
        sm.sendEvent(msg);
    }

    // build a State Machine based on the current State of a payment retrieved from the database
    // this is the base method to perform all State Machine methods -> authorize, decline, etc.

    // since we're persisting the state of a payment through a state machine, we're basically checking the
    // state of the State Machine in and out of the database at each step/state change
    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {

        Payment payment = paymentRepository.getOne(paymentId);

        // generate a State Machine based on the paymentId of the payment event
        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        // tell State Machine to stop
        sm.stop();

        // and set State Machine State to the State value we retrieve from the database
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    // add interceptor/listener using PaymentStateChangeInterceptor object
                    // so before we reset the State Machine, we're gonna grab the current state of the event and persist it back to the database
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        // restart the State Machine with the new State from database
        sm.start();

        return sm;
    }
}
