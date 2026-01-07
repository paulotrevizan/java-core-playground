# Design Decisions & Trade-offs — Java Core Playground

## User (Model)
- **Immutable**: all fields are `final`, no setters.
- **Thread-safe**: safe to share instances across threads.
- **WithId pattern**: creates new instance with updated `id` instead of mutating.
- **Trade-offs**:
  - Pros: avoids race conditions, side-effect free, easy reasoning.
  - Cons: small memory overhead when creating new instance.

## UserRepository
- **In-memory storage**: `ConcurrentHashMap<Long, User>`.
- **ID generation**: `AtomicLong` ensures unique IDs thread-safely.
- **findAll() returns new ArrayList**: defensive copy to prevent external mutation.
- **Trade-offs**:
  - Pros: thread-safe, simple, predictable.
  - Cons: not persistent, limited scalability, memory overhead for copies.

## UserService
- **Layered responsibility**: handles business logic and simple validation only.
- **Validation**: ensures `name` and `email` are not null before saving.
- **Exception handling**:
  - Throws domain-specific runtime exceptions (`InvalidUserException`, `UserNotFoundException`).
  - Converts `Optional` results from the repository into domain errors.
- **Trade-offs**:
  - Pros: separates concerns, keeps repository simple, explicit domain rules, clear failure modes, easier testing.
  - Cons: requires defining additional exception classes.

## UserController
- **Controller thin**: delegates all business logic to `UserService`.
- **Endpoints implemented**:
  - `GET /users`: returns all users
  - `GET /users/{id}`: returns one user or propagates domain exception
  - `POST /users`: creates a new user, returns 200 OK (for now)
- **Exception handling**:
  - No try/catch blocks.
  - Relies on global exception handling via `@ControllerAdvice`.
- **DTO vs Entity**: using entity directly for now, no separate DTO layer.
- **Trade-offs**:
  - Pros: simple, keeps controller focused on routing, fast to prototype.
  - Cons:
    - POST ideally should return `201 Created` with Location header (TO-DO).
    - Tightly couples API to internal entity, future changes will require DTOs.
- **Scalability / maintenance**: sufficient for a small learning POC and allows fast iteration and focus on core Java/Spring concepts.

## UserService Tests
- **Light-weight unit tests**: cover basic behavior of `createUser`, `getUserById` and `getAllUsers`.
- **No complex mocks**: repository is in-memory, keep it simple for now.
- **Trade-offs**:
  - Pros: ensures correct behavior and facilitates future refactoring, providing fast feedback for future changes.
  - Cons:
    - Does not cover real concurrency and it is still not integrated with a real DB.
    - Does not validate HTTP layer behavior.
- **Testing decisions**:
  - Focus on **behavior**: success and failure paths.
  - Do not test framework or DB integration (future implementation will cover it).

## Exceptions & Error Handling
- The project uses **unchecked exceptions** for domain errors.
- Domain-specific exceptions clearly express business rules:
  - `InvalidUserException` for invalid input.
  - `UserNotFoundException` for missing resources.
- **Repository layer**:
  - Returns `Optional` to represent absence without enforcing policy.
- **Service layer**:
  - Converts `Optional` into domain exceptions.
  - Owns the decision of what constitutes an error.
- **Controller layer**:
  - Does not handle exceptions directly.
  - Delegates error-to-HTTP translation to `@ControllerAdvice`.
- **Checked exceptions are avoided**:
  - To prevent method signature pollution.
  - Because domain errors are not recoverable at this layer.

## General Notes
- **Immutability & thread-safety**: using immutable models avoids synchronized for read-only operations.
- **Performance considerations**: defensive copies and creation of new instances have minor memory/CPU overhead, acceptable for small-scale and learning projects.
- **Scalability**: current in-memory approach is fine for the initial POC and it'll be replaced with real DB later.
