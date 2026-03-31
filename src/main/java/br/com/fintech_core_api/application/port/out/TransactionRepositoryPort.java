package br.com.fintech_core_api.application.port.out;

import br.com.fintech_core_api.domain.entity.Transaction;

public interface TransactionRepositoryPort {
    void save (Transaction transaction);
}


