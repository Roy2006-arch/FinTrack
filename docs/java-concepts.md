# Java and OOP evaluation map

Paths below are relative to `backend/src/main/java/com/fintrack/`.

| Concept | Concrete example | Viva explanation |
|---|---|---|
| Classes and objects | `entity/Transaction.java`; `new Transaction()` in transaction service | The class defines state/behavior; each object represents one transaction. |
| Encapsulation | Private entity fields with public accessors | State is hidden behind an explicit API; mutation is coordinated by the service. |
| Abstraction | `service/TransactionService.java` | Callers know supported operations without depending on persistence implementation. |
| Interface | `TransactionService`; repository interfaces | Interfaces specify contracts. Spring creates repository implementations at runtime. |
| Polymorphism | `TransactionController` holds `TransactionService`, injected with `TransactionServiceImpl` | Code targets a contract; runtime selects the implementation. Mockito can substitute it in tests. |
| Inheritance | `User`, `Category`, `Transaction`, `Budget`, `FinancialGoal` extend `BaseEntity` | Common identity/audit state has a meaningful parent; not an artificial income/expense hierarchy. |
| Constructors | User/Category constructors; constructor injection in services | Constructors establish required dependencies/state; JPA also requires a no-argument constructor. |
| Overloading | `CategoryService.require(uid,id)` and `require(uid,id,type)` | Same method name with different parameters adds type compatibility checking when needed. |
| Overloading | `AnalyticsService.report(uid,YearMonth)` and `report(uid,start,end)` | Calendar-month convenience delegates to the date-range calculation. |
| Overriding | `TransactionServiceImpl` methods; JWT filter's `doFilterInternal`; seeder `run` | A class provides behavior required by an interface or superclass. |
| Exceptions | `BadRequestException`, `ConflictException`, `ResourceNotFoundException` | Domain failures carry meaningful messages and are mapped centrally to HTTP responses. |
| Exception handling | `GlobalExceptionHandler`; JWT filter catch block | Controlled errors do not expose internal stack traces; unexpected errors are logged server-side. |
| Collections | List of DTOs; Map of category totals; Set of permitted sort values | List keeps ordered results; Map associates category IDs with totals; Set supports membership tests. |
| Generics | `JpaRepository<Transaction,Long>`; `PageView<T>` | Generic types preserve compile-time type safety and reuse common structures. |
| Streams | `BudgetService.list`, `AnalyticsService.summary`, `FinanceMapper` callers | Transform repository data and reduce monetary values without materializing unrelated records. |
| Lambdas | `map(b -> ...)`, `orElseThrow(() -> ...)`, query predicates | Concise functional behavior passed to library operations. |
| Method references | `FinanceMapper::transaction`, `BigDecimal::add`, `BudgetView::amount` | Refer to existing behavior where a functional interface is expected. |
| Optional | Scoped `findByIdAndUserId(...).orElseThrow(...)` | Makes absence explicit rather than returning null. |
| Enumerations | `TransactionType`, `PaymentMethod`, `CategoryType` | Closed sets prevent invalid string states; persisted with enum names, not unstable ordinal positions. |
| Records | Nested immutable request/view records | Concise immutable boundary values with generated constructors/accessors/equality. |
| BigDecimal | `FinancialMath`, report calculations and entity amounts | Decimal arithmetic avoids binary floating-point surprises in money calculations. |
| File/I/O handling | `CsvExportService`, `Writer`, `OutputStream`, UTF-8 | Streams real CSV bytes to HTTP clients, escapes cells and processes bounded pages. |
| Date/time | Instant audit fields; LocalDate business dates; YearMonth budgets | Each Java time type expresses a different domain meaning. |
| Validation | `Requests.java` Jakarta annotations plus service rules | Structural validation belongs at the API boundary; contextual/ownership rules belong in services. |
| Dependency injection | Constructor parameters in every controller/service | Spring constructs collaborating objects, making dependencies explicit and testable. |
| JPA | Entity relationships and repositories | Standard API for mapping Java objects and relational data. |
| Hibernate | Boot's default JPA provider | Implements ORM, SQL generation, lazy associations and dirty checking. |
| JDBC | MySQL Connector/J and HikariCP behind Hibernate | SQL reaches MySQL through JDBC connections; ORM builds on JDBC rather than replacing the database. |
| Transaction boundaries | `@Transactional` write/read-only service methods | Operations commit or roll back together; a failed seed or registration does not leave half-created data. |
| Spring Boot | `FinTrackApplication`, starters, auto-configuration | Wires framework components and embedded Tomcat around a small executable entry point. |
| REST | Eight controller classes | HTTP resources, DTO JSON, standard methods and status codes. |
| Security | BCrypt, JWT signature validation and scoped repository reads | Password hashing, authentication and authorization solve different problems. |
| Background processing | Hourly `AuthService.removeExpiredSessions` | Spring's scheduler performs genuinely useful session housekeeping; no artificial threaded money calculations. |
| Unit testing | `AuthServiceTest`, `TransactionServiceTest`, math/analytics tests | Mockito isolates dependencies; JUnit asserts observable rules rather than implementation trivia. |

## Good source walkthrough

1. Start at `TransactionController.create` and explain `@Valid`, the JWT principal and 201 status.
2. Follow the injected `TransactionService` to its implementation.
3. Explain `CategoryService.require` ownership and overloaded type validation.
4. Follow the entity relationship to `Category`/`User` and the repository save.
5. Show the mapped `TransactionView`, which has no entity cycles or passwords.
6. Demonstrate `FinancialMath.percentage` and threshold tests at 75, 90 and 100.
7. Finish with a negative ownership test and a revoked-token integration test.

One generic resource-not-found exception is intentionally used instead of five identical subclasses: its constructor supplies the resource name, while status/behavior are identical. This avoids artificial hierarchy solely for a checklist.
