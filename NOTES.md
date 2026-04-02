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

## 30/03/2026 — O Coração do Hexágono: Ports & Adapters

**Foco:** Isolamento da lógica de negócio e Inversão de Dependência.

Nesta etapa, saímos do `domain` e entramos na camada de `application`. O objetivo aqui é definir como o mundo se comunica com o nosso sistema sem "sujar" o código com detalhes de banco de dados ou frameworks.

### 1. O que são Ports?
As "Ports" (Portas) são interfaces que servem como a fronteira do nosso sistema. Elas definem o contrato de comunicação.
- **Portas de Entrada (Inbound):** Definem o que o sistema **pode fazer**. O Controller (Web) chama essa porta.
- **Portas de Saída (Outbound):** Definem o que o sistema **precisa para funcionar**. O sistema chama essa porta para buscar ou salvar dados.

### 2. Inversão de Dependência (O "D" do SOLID) na prática
Em vez de o nosso serviço depender do `JpaRepository` (que é um detalhe do Spring/PostgreSQL), ele depende da nossa interface `WalletRepositoryPort`.
- **Por que isso é incrível?** Porque o Core da aplicação (o Hexágono) não conhece o banco de dados. Se amanhã trocarmos o PostgreSQL pelo MongoDB, o `domain` e o `application` permanecem intactos. Só precisamos criar um novo "Adapter" que implemente a porta.

### 3. O uso de Commands e Records
Para passar dados para o Caso de Uso, usamos o `ProcessPaymentCommand` (Java Record).
- **Imutabilidade:** Como é um Record, os dados que entram no sistema não podem ser alterados acidentalmente.
- **Flexibilidade:** Se o pagamento precisar de novos dados (como uma descrição ou data agendada), alteramos o Command sem quebrar a assinatura dos métodos existentes em toda a aplicação.

### 4. Fluxo de Dependência
O fluxo sempre deve ser de **FORA para DENTRO**:
`Web (Controller) -> Input Port -> Use Case (Service) -> Output Port -> Database (Adapter)`

## 01/04/2026 — O Maestro (Application Service)

**Foco:** Orquestração de Casos de Uso sem vazamento de regras de negócio.

- **Responsabilidade do Service:** O Caso de Uso (`PaymentService`) funciona como um maestro. Ele não conhece a matemática do negócio, apenas o fluxo da operação:
    1. Recuperar estado (via Portas de Saída).
    2. Modificar o estado (chamando métodos das Entidades do Domínio).
    3. Persistir o estado (via Portas de Saída).
- **Consistência e Transação:** O uso da anotação `@Transactional` na borda do Caso de Uso garante que a operação seja atômica. Se a entidade lançar uma exceção de negócio ou o banco falhar, nada é salvo e a integridade da base de dados é mantida.
- **Injeção de Dependências:** Sempre via construtor para as `Ports`. Facilita testes unitários utilizando mocks (ex: Mockito) sem depender do Spring Container.
