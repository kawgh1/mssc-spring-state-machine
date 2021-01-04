package com.kwgdev.msscspringstatemachine.domain;

/**
 * created by kw on 1/4/2021 @ 6:34 AM
 */
public enum PaymentEvent {
    PRE_AUTHORIZE, PRE_AUTH_APPROVED, PRE_AUTH_DECLINED, AUTHORIZE, AUTH_APPROVED, AUTH_DECLINED
}
