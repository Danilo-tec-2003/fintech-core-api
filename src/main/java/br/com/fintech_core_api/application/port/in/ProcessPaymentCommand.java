package br.com.fintech_core_api.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

// O record é perfeito aqui: imutável e serve apenas para transportar dados
// do Controller para o UseCase
public record ProcessPaymentCommand(
        UUID sourceWalletId,
        UUID targetWalletId,
        BigDecimal amount
) {}
