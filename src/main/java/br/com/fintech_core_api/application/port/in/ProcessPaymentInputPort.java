package br.com.fintech_core_api.application.port.in;

public interface ProcessPaymentInputPort {
    java.util.UUID execute (ProcessPaymentCommand command);
}
