## 14/03/2026 — Estrutura inicial do projeto

**Foco:** Arquitetura de pacotes com Clean Architecture

**O que cada camada faz:**

- `domain/` — coração do sistema. Java puro, zero dependências externas.
  Aqui vivem as regras de negócio reais. Não sabe que Spring existe.
    - `entity/` — objetos do mundo real: Transaction, Wallet, User
    - `exception/` — erros de negócio: InsufficientBalanceException
    - `usecase/` — o que o sistema faz: ProcessPayment, Refund

- `application/` — orquestra os casos de uso. Conhece o domínio,
  não conhece banco nem HTTP.
    - `port/in/` — interfaces do que o mundo pode pedir ao sistema
    - `port/out/` — interfaces do que o sistema precisa do mundo externo
    - `service/` — implementa os ports de entrada usando o domínio

- `infrastructure/` — detalhes técnicos. Banco, Kafka, AWS, HTTP.
  Pode ser trocado sem tocar no domínio.
    - `persistence/` — JPA, PostgreSQL, adapters de repositório
    - `messaging/kafka/` — producer e consumer de eventos
    - `web/` — controllers REST, DTOs, handler de erros

- `config/` — configurações do Spring: Kafka, AWS, Security
------------------------------------------------------------------------------------------------

## Onde usar e onde NÃO usar Spring

**Regra única:** Spring entra na `infrastructure` e `config`. Nunca no `domain`.

| Package | Spring? |
|---|---|
| `domain/` (entity, exception, usecase) | ❌ Java puro |
| `application/port` (interfaces) | ❌ Java puro |
| `application/service` | ✅ @Service |
| `infrastructure/persistence` | ✅ @Entity, @Repository |
| `infrastructure/web` | ✅ @RestController |
| `infrastructure/messaging` | ✅ @KafkaListener |
| `config` | ✅ @Configuration, @Bean |

**Por quê?**
O domínio não sabe que o Spring existe. Se amanhã trocar
Spring por Quarkus, ou PostgreSQL por MongoDB — o domain
não muda uma linha. Só reescreve os adapters.

**Para fixar:** Transaction.java = Java puro.
TransactionJpaEntity.java = @Entity na infrastructure.
São duas classes separadas conectadas por um adapter.


## 30/03/2026 — Máquina de Estados no Domínio

**Foco:** Controle seguro do ciclo de vida da entidade.

**Conceito principal:** Quando uma entidade passa por diferentes fases (ex: `PENDING` -> `APPROVED`), as regras de transição (State Machine) devem pertencer à própria entidade, e não a um serviço externo.

**Na prática (O caso da `Transaction`):**
1. **Estado inicial blindado:** O construtor define rigidamente `status = PENDING`. Nenhuma transação pode ser criada já aprovada ou falha.
2. **Transições seguras:** Métodos de ação (`approve()`, `refund()`) verificam o estado atual antes de aplicar o novo. Isso impossibilita anomalias técnicas, como tentar estornar uma transação que nem foi aprovada.
