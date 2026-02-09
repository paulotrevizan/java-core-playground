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
  - `GET /users`: returns all users.
  - `GET /users/{id}`: returns one user or propagates domain exception.
  - `POST /users`: creates a new user, now returns `201 Created` with `Location` header.
  - `PUT /users/{id}`: updates an existing user.
  - `DELETE /users/{id}`: deletes an existing user.
- **DTO vs Entity**: using `UserDto` in controller to decouple API contract from internal entity.
- **Exception handling**:
  - No try/catch blocks.
  - Relies on global exception handling via `@ControllerAdvice`.
- **Trade-offs**:
  - Pros: clear HTTP semantics, separates API model from domain, full CRUD supported.
  - Cons: service layer still works with entity, DTO mapping added, minor extra complexity.

## ExternalUserValidationClient
- **Responsibility**: integrates with external user validation service over HTTP.
- **Implementation**:
  - Uses `RestTemplate` with Apache HttpClient.
  - Base URL fixed for tests: `http://localhost:8099`.
- **Error handling**:
  - Throws `ExternalServiceException` on:
    - null response
    - client/server errors (4xx, 5xx)
    - timeouts (`SocketTimeoutException` wrapped)
- Error mapping:
  - `ResourceAccessException` - `ExternalServiceException`
  - `SocketTimeoutException` - `ExternalServiceException` with timeout message
- Trade-offs:
  - Pros: explicit, testable, covers slow responses
  - Cons: no retries, no circuit breakers, fixed base URL in tests

## UserService Tests
- **Light-weight unit tests**: cover basic behavior of `createUser`, `getUserById`, `updateUser`, `deleteUser`, and `getAllUsers`.
- **No complex mocks**: repository is in-memory, keep it simple for now.
- **Trade-offs**:
  - Pros: ensures correct behavior and facilitates future refactoring, providing fast feedback for changes.
  - Cons:
    - Does not cover real concurrency and is still not integrated with a real DB.
    - Does not validate HTTP layer behavior.

## UserController Tests
- **Integration-style tests**: use MockMvc to validate HTTP status, payload structure, JSON binding, and endpoint contracts.
- **Do not test service logic**: only verifies the API layer behaves as expected.
- **Trade-offs**:
  - Pros: ensures HTTP contract is correct, catches serialization/validation issues, safe to refactor service logic.
  - Cons: requires Spring context load, slightly slower than pure unit tests.

### Integration tests (WireMock)
- **Purpose**: validate client behavior in success and failure scenarios.
- **Scenarios**:
  - **Success (`200 OK`)**
    - WireMock returns `{ "valid": true }`.
    - Client returns `true`.
  - **Server error (`500`)**
    - WireMock returns 500.
    - Client throws `ExternalServiceException`.
  - **Intermittent server error**
    - WireMock returns 500 on first call, then 200 on second.
    - Client throws on first call, succeeds on second.
  - **Timeout**
    - WireMock delays response beyond RestTemplate timeout (e.g., 5s).
    - Client throws `ExternalServiceException` with timeout message.
- **Trade-offs**:
  - Pros: Covers main failure cases, reproducible locally.
  - Cons: No circuit breaker or retry logic, only for testing.

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
