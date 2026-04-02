package br.com.fintech_core_api.domain.entity;

import br.com.fintech_core_api.domain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private UUID sourceWalletId = null; //quem paga
    private final UUID targetWalletId; //quem recebe
    private final BigDecimal amount;
    private TransactionStatus status;
    private final LocalDateTime createdAt;

    public Transaction (UUID id, UUID targetWalletId, UUID walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("O valor da transação deve ser maior que zero.");
        }
        if (sourceWalletId.equals(targetWalletId)) {
            throw new InvalidTransactionException("A carteira de origem e destino não podem ser as mesmas.");
        }

        this.id = id;
        this.sourceWalletId = sourceWalletId;
        this.targetWalletId = targetWalletId;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void approve() {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Apenas transações pendentes podem falhar.");
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void fail() {
        if(this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Apenas transações pendentes podem falhar.");
        }
        this.status = TransactionStatus.FAILED;
    }

    public void refund() {
        if(this.status != TransactionStatus.APPROVED) {
            throw new InvalidTransactionException("Apenas transações aprovadas podem ser estornadas.");
        }
        this.status = TransactionStatus.REFUNDED;
    }

    public UUID getId() {
        return id;
    }
    public UUID getSourceWalletId() {
        return sourceWalletId;
    }
    public UUID getTargetWalletId () {
        return targetWalletId;
    }
    private BigDecimal getAmount() {
        return amount;
    }
    private TransactionStatus getStatus() {
        return status;
    }
    private LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
