package br.com.fintech_core_api.domain.entity;

import br.com.fintech_core_api.domain.exception.InsufficientBalanceException;
import br.com.fintech_core_api.domain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.util.UUID;

public class Wallet {

    private final UUID id;
    private final UUID userId;
    private BigDecimal balance;

    public Wallet (UUID id, UUID userId, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <=0) {
            throw new IllegalArgumentException("O valor do débito deve ser maior que zero.");
        }
        if (this.balance.compareTo(amount) <0) {
            throw new InsufficientBalanceException("Saldo insuficiente na carteira.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <=0) {
            throw new InvalidTransactionException("O valor do crédito deve ser maior que zero.");
        }
        this.balance = this.balance.add(amount);
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public BigDecimal getBalance() {
        return balance;
    }





}
