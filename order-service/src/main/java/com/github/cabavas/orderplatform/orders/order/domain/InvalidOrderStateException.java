package com.github.cabavas.orderplatform.orders.order.domain;

public class InvalidOrderStateException extends RuntimeException{
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
