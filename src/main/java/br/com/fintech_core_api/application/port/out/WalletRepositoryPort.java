package br.com.fintech_core_api.application.port.out;

import br.com.fintech_core_api.domain.entity.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepositoryPort {
    Optional<Wallet> findById(UUID id);
    void save(Wallet wallet);
}
