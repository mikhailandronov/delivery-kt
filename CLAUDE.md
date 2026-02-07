# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a delivery management system built with Kotlin, implementing **Domain-Driven Design (DDD) with Hexagonal Architecture**. The course project demonstrates clean architecture principles with clear separation between domain logic, infrastructure, and application layers.

## Build and Run Commands

### Running the Application
```bash
./gradlew run              # Run the CLI application
gradlew.bat run            # Windows equivalent
```

### Testing
```bash
./gradlew test                         # Run all tests
./gradlew :core:test                   # Test specific module
./gradlew :infrastructure:test         # Test infrastructure layer
./gradlew test --tests "OrderTest"     # Run specific test class
./gradlew test --tests "*dispatch*"    # Run tests matching pattern
```

### Build
```bash
./gradlew build             # Build all modules
./gradlew build -x test     # Build without running tests
./gradlew clean             # Clean build artifacts
```

## Architecture

### Module Structure

```
delivery-kt/
├── app/              # Application entry point (CLI), depends on api
├── api/              # API interfaces (HTTP, Kafka adapters), depends on core
├── core/             # Domain logic (pure business rules), no dependencies on other modules
└── infrastructure/   # Technical implementations (DB, messaging), depends on core
```

**Dependency Rule:** Dependencies flow inward only. Infrastructure depends on core, not vice versa. Core contains pure domain logic with no external dependencies.

### Domain Layer (core/)

**Entities:** `Order`, `Courier`, `StoragePlace`
- Aggregates with business rules and invariant enforcement
- Use `Either<Error, Entity>` pattern for error handling (Arrow)
- State transitions validated via domain methods

**Domain Services:** `IDispatchService`, `DispatchService`
- Business logic that doesn't naturally belong to an entity
- `DispatchService` assigns orders to nearest available couriers

**Value Objects:** `Location`, `Speed`, `Volume`, `Name`
- Immutable types with validation

**Ports (interfaces in `core/ports/outbound/`):**
- `ICourierRepository`, `IOrderRepository` - Repository contracts
- `ITransactionManager` - Transaction management contract

### Infrastructure Layer (infrastructure/)

**Implementations:**
- `KtormCourierRepository`, `KtormOrderRepository` - Ktorm ORM implementations
- `KtormTransactionManager` - Transaction management
- `KtormSchemaDefinitions` - Database schema mapping

**Database:** Uses Ktorm ORM with PostgreSQL (production) or H2 (testing)

### Testing Infrastructure

Uses **Kotest** with **Koin** for dependency injection:
- Test modules in each module's test source set
- `core/test/kotlin/org/ama/delivery/di/testModules.kt` - Core DI setup
- `infrastructure/test/kotlin/org/ama/delivery/infrastructure/di/testModules.kt` - Infrastructure DI setup

## Key Conventions

### Error Handling
All domain operations use Arrow's `Either` type:
```kotlin
fun assignOrder(order: Order): Either<OrderError, Order>
```

Error types are sealed classes in each domain package:
- `OrderError`, `CourierError`, etc.

### Naming
- Interface names prefixed with `I`: `ICourierRepository`
- Repository implementations prefixed with ORM name: `KtormCourierRepository`
- Main class: `org.ama.delivery.app.AppKt`

### Dependency Injection
Koin modules define dependencies. Tests use separate test modules with in-memory/mock implementations.

## Technology Stack

- **Kotlin** 2.1.21, targeting JVM 21
- **Arrow** 2.1.2 for functional programming
- **Koin** 4.0.3 for dependency injection
- **Ktorm** 4.1.1 for ORM
- **Kotest** 6.0.0.M4 for testing
- **PostgreSQL** 42.7.7 / **H2** 2.3.232 for databases

## Build System

Uses Gradle with Kotlin DSL:
- Centralized build logic in `build-logic/`
- Convention plugin: `kotlin-basic-convention`
- Version catalog: `gradle/libs.versions.toml`

All modules apply the `kotlin-basic-convention` plugin for standard Kotlin compilation setup.
