# 💸  — Fintech Core API

> API de processamento de pagamentos construída do zero com Java 17, Spring Boot 3, Kafka e AWS.
> Projeto de estudo aplicado com foco em Clean Architecture, SOLID e Clean Code.

---

## 📌 Sobre

 simula o core de uma fintech real: criação de transações, validação de saldo, processamento assíncrono via Kafka, notificações por AWS SQS e armazenamento de comprovantes no S3.

O projeto é construído publicamente como jornada de aprendizado — cada módulo entregue vira um post no LinkedIn documentando o conceito aprendido, o problema que ele resolve e o código antes/depois.

---

## 🏗️ Arquitetura

O projeto segue **Clean Architecture** com o padrão **Ports & Adapters**.

```
src/main/java/br/com/fintech_core_api/
│
├── domain/                    # Núcleo — Java puro, zero dependências externas
│   ├── entity/                # Objetos do negócio: Transaction, Wallet, User
│   ├── exception/             # Erros de negócio: InsufficientBalanceException
│   └── usecase/               # O que o sistema faz: ProcessPayment, Refund
│
├── application/               # Orquestra os casos de uso
│   ├── port/
│   │   ├── in/                # Interfaces do que o mundo pode pedir ao sistema
│   │   └── out/               # Interfaces do que o sistema precisa do mundo externo
│   └── service/               # Implementa os ports de entrada usando o domínio
│
├── infrastructure/            # Detalhes técnicos — banco, Kafka, AWS, HTTP
│   ├── persistence/
│   │   ├── entity/            # Entidades JPA (separadas do domínio)
│   │   ├── repository/        # Interfaces Spring Data
│   │   └── adapter/           # Conecta o domínio ao banco de dados
│   ├── messaging/
│   │   └── kafka/
│   │       ├── producer/      # Publica eventos de pagamento
│   │       └── consumer/      # Consome e processa eventos
│   └── web/
│       ├── controller/        # Endpoints REST
│       ├── dto/
│       │   ├── request/       # Objetos de entrada da API
│       │   └── response/      # Objetos de saída da API
│       └── handler/           # Tratamento global de erros
│
└── config/                    # Configurações Spring: Kafka, AWS, Security
```

### Princípio fundamental

O `domain` não importa nada do Spring, do Kafka, nem do banco de dados. Frameworks e cloud são detalhes de infraestrutura — plugáveis e substituíveis. Se amanhã trocar PostgreSQL por MongoDB, ou Spring por Quarkus, o domínio não muda uma linha.

```
domain        →  Java puro. Nenhuma anotação de framework.
application   →  @Service. Conhece o domínio, não conhece banco nem HTTP.
infrastructure → @Entity, @RestController, @KafkaListener. Detalhes técnicos.
config        →  @Configuration, @Bean. Setup do Spring.
```

---

## 🔄 Fluxo de uma transação

```
POST /api/v1/payments
        │
        ▼
PaymentController (@RestController)
        │
        ▼
ProcessPaymentInputPort (interface — application/port/in)
        │
        ▼
PaymentService (@Service)
        │
        ├──► ProcessPaymentUseCase (domínio puro)
        │           │
        │           └──► Transaction.create() + Wallet.debit/credit()
        │
        ├──► TransactionRepositoryPort ──► PostgreSQL
        │
        └──► PaymentEventPublisherPort ──► Kafka (topic: payment.processed)
                                                │
                                                ▼
                                    KafkaPaymentEventConsumer
                                                │
                                                ├──► AWS SQS (notificação)
                                                └──► AWS S3 (comprovante PDF)
```

---

## ⚙️ Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2.x |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Mensageria | Apache Kafka |
| Fila | AWS SQS |
| Storage | AWS S3 |
| Testes | JUnit 5 + Testcontainers |
| Documentação | SpringDoc (Swagger) |
| CI/CD | GitHub Actions |
| Deploy | AWS EC2 + Docker |
| Monitoramento | AWS CloudWatch |

---

## 🧱 SOLID na prática

| Princípio | Onde aparece no projeto |
|---|---|
| **S** — Single Responsibility | `PaymentService` só processa pagamentos. `WalletService` só gerencia saldo. |
| **O** — Open/Closed | Novo tipo de pagamento → nova classe. Nenhuma existente é modificada. |
| **L** — Liskov Substitution | Qualquer `RepositoryAdapter` pode substituir outro sem quebrar a aplicação. |
| **I** — Interface Segregation | `ProcessPaymentInputPort` e `GetTransactionInputPort` são interfaces separadas. |
| **D** — Dependency Inversion | `PaymentService` depende de interfaces, nunca de implementações JPA concretas. |

---

## 📦 Roadmap

### Módulo 1 — Fundação (Semanas 1–3)
Domínio puro, casos de uso, repositório com PostgreSQL, endpoints REST, testes unitários.

- [ ] Entidades de domínio: `Transaction`, `Wallet`, `User`
- [ ] Exceptions de negócio customizadas
- [ ] Casos de uso: `ProcessPaymentUseCase`, `RefundTransactionUseCase`
- [ ] Ports de entrada e saída
- [ ] Adapter JPA com PostgreSQL
- [ ] Migrations com Flyway
- [ ] Endpoints REST com validação
- [ ] `GlobalExceptionHandler`
- [ ] Swagger configurado
- [ ] Testes unitários do domínio (sem Spring, sem banco)

### Módulo 2 — Mensageria (Semanas 4–6)
Kafka, eventos assíncronos, Testcontainers.

- [ ] Kafka no docker-compose
- [ ] `KafkaPaymentEventProducer` — publica evento após pagamento aprovado
- [ ] `KafkaPaymentEventConsumer` — processa evento e dispara notificação
- [ ] Dead Letter Queue para mensagens com falha
- [ ] Testes de integração com Testcontainers (Kafka + PostgreSQL reais)

### Módulo 3 — Cloud AWS (Semanas 7–9)
SQS, S3, IAM.

- [ ] AWS SQS — notificação assíncrona ao usuário
- [ ] AWS S3 — upload de comprovante PDF após pagamento
- [ ] Endpoint de download via URL pré-assinada do S3
- [ ] Testes com LocalStack

### Módulo 4 — Produção (Semanas 10–12)
Deploy, CI/CD, observabilidade.

- [ ] Deploy na EC2 com Docker
- [ ] GitHub Actions: build → testes → deploy automático
- [ ] CloudWatch para logs estruturados
- [ ] Revisão final com Clean Code e SOLID

---

## 🚀 Como rodar localmente

### Pré-requisitos
- Java 17+
- Docker e Docker Compose

### Subindo o ambiente

```bash
# Clone o repositório
git clone https://github.com/Danilo-tec-2003/.git
cd 

# Sobe PostgreSQL + Kafka + Zookeeper + Kafka UI
docker-compose up -d

# Roda a aplicação
./mvnw spring-boot:run
```

### Acessando

| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Kafka UI | http://localhost:8090 |

---

## 🧪 Testes

```bash
# Unitários — sem nenhuma infraestrutura, roda em segundos
./mvnw test

# Integração — sobe containers reais via Testcontainers
./mvnw verify -P integration-tests
```

### Cobertura mínima por camada

| Camada | Cobertura |
|---|---|
| `domain` | 90%+ |
| `application` | 80%+ |
| `infrastructure` | 60%+ |

---

## 📬 Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/payments` | Processa um pagamento |
| `GET` | `/api/v1/payments/{id}` | Consulta uma transação |
| `POST` | `/api/v1/payments/{id}/refund` | Estorna um pagamento |
| `GET` | `/api/v1/wallets/{userId}` | Consulta saldo da carteira |
| `GET` | `/api/v1/payments/{id}/receipt` | Download do comprovante (S3) |

---

## 📝 Jornada de aprendizado

Este projeto é construído publicamente. A cada módulo, um post no LinkedIn documenta:

- O conceito aprendido
- O problema real que ele resolve
- Código antes/depois
- Erros cometidos e como foram corrigidos

Acompanhe: [LinkedIn — Danilo Mendes](https://www.linkedin.com/in/danilo-mendes)

O processo de aprendizado está registrado em [`NOTES.md`](./NOTES.md).

---

## 👤 Autor

**Danilo Mendes Araújo**
Desenvolvedor Back-End | Java · Spring Boot · PostgreSQL

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Danilo%20Mendes-blue?style=flat&logo=linkedin)](https://www.linkedin.com/in/danilo-mendes)
[![GitHub](https://img.shields.io/badge/GitHub-Danilo--tec--2003-gray?style=flat&logo=github)](https://github.com/Danilo-tec-2003)