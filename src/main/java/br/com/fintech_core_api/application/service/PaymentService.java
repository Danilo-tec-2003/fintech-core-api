package br.com.fintech_core_api.application.service;

import br.com.fintech_core_api.application.port.in.ProcessPaymentCommand;
import br.com.fintech_core_api.application.port.in.ProcessPaymentInputPort;
import br.com.fintech_core_api.application.port.out.TransactionRepositoryPort;
import br.com.fintech_core_api.application.port.out.WalletRepositoryPort;
import br.com.fintech_core_api.domain.entity.Transaction;
import br.com.fintech_core_api.domain.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService implements ProcessPaymentInputPort {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    // Injeção de dependência via construtor (Best practice do Spring)
    public PaymentService(WalletRepositoryPort walletRepositoryPort,
                          TransactionRepositoryPort transactionRepositoryPort) {
        this.walletRepositoryPort = walletRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    @Transactional
    public UUID execute(ProcessPaymentCommand command) {

        // 1. Carrega o estado atual (Buscando no banco via Port)
        Wallet sourceWallet = walletRepositoryPort.findById(command.sourceWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira de origem não encontrada."));

        Wallet targetWallet = walletRepositoryPort.findById(command.targetWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira de destino não encontrada."));

        // 2. Cria a Transação (Domínio garante que ela nasce PENDENTE e com valores válidos)
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                sourceWallet.getId(),
                targetWallet.getId(),
                command.amount()
        );

        // 3. Executa a Lógica de Negócio (O Service apenas orquestra, as Entidades se validam)
        sourceWallet.debit(command.amount());
        targetWallet.credit(command.amount());
        transaction.approve();

        // 4. Persiste o novo estado (Salvando no banco via Port)
        walletRepositoryPort.save(sourceWallet);
        walletRepositoryPort.save(targetWallet);
        transactionRepositoryPort.save(transaction);

        // 5. Retorna a resposta
        return transaction.getId();
    }
}