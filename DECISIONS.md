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
- **Layered responsibility**: handles business logic / simple validation only.
- **Validation**: ensures `name` and `email` are not null before saving.
- **Exception handling**: throws RuntimeException when user not found or invalid.
- **Trade-offs**:
    - Pros: separates concerns, keeps repository simple, easier to test.
    - Cons: exceptions are generic (will be improved with custom exceptions later).

## UserController
- **Controller thin**: delegates all business logic to `UserService`.
- **Endpoints implemented**:
    - `GET /users`: returns all users
    - `GET /users/{id}`: returns one user or throws `RuntimeException` if not found
    - `POST /users`: creates a new user, returns 200 OK (for now)
- **Exception handling**: minimal; uses `RuntimeException` for domain errors.
- **DTO vs Entity**: using entity directly for now, no separate DTO layer.
- **Trade-offs**:
    - Pros: simple, keeps controller focused on routing, fast to prototype.
    - Cons:
        - POST ideally should return `201 Created` with Location header (TO-DO).
        - GET /users/{id} throws generic 500 instead of 404, will refine with `@ControllerAdvice` later.
        - Tightly couples API to internal entity, future changes will require DTOs.
- **Scalability / maintenance**: sufficient for a small learning POC and allow fast iteration and focus on core Java/Spring concepts.

## UserService Tests
- **Light-weight unit tests**: cover basic behavior of `createUser`, `getUserById` and `getAllUsers`.
- **No complex mocks**: repository is in-memory, keep it simple for now.
- **Trade-offs**:
    - Pros: ensures correct behavior and facilitates future refactoring, providing fast feedback for future changes.
    - Cons: does not cover real concurrency and it is still not integrated with a real DB, tests do not isolate specific exceptions (generic RuntimeException).
- **Testing decisions**:
    - Focus on **behavior**: success and failure paths.
    - Do not test framework or DB integration (future implementation will cover it).

## General Notes
- **Immutability & thread-safety**: using immutable models avoids synchronized for read-only operations.
- **Performance considerations**: defensive copies and creation of new instances have minor memory/CPU overhead, acceptable for small-scale and learning projects.
- **Scalability**: current in-memory approach is fine for the initial POC and it'll be replaced with real DB later.
