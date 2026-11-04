# Viva preparation — 120 answered questions

## A. Java and OOP — 30 questions

1. **What is a class?** A definition of an object's state and behavior; `Transaction` defines a finance entry.
2. **What is an object?** A runtime instance of a class, such as one saved expense.
3. **What is encapsulation?** Hiding state behind controlled access; entities use private fields and accessors.
4. **What is abstraction?** Exposing a useful contract while hiding implementation; `TransactionService` exposes operations.
5. **What is inheritance?** Reusing/extending a parent class; financial entities inherit identity and timestamps from `BaseEntity`.
6. **What is polymorphism?** One contract with runtime-specific behavior; the controller invokes a service implementation through its interface.
7. **What is an interface?** A contract of supported methods; it is not itself the business implementation.
8. **What is a constructor?** Initialization code invoked when an object is created; services use it to receive required collaborators.
9. **Why do JPA entities have no-argument constructors?** The persistence provider needs them to instantiate entities; additional constructors can improve normal application use.
10. **What is method overloading?** Same method name, different parameters; report methods accept a YearMonth or explicit start/end dates.
11. **What is method overriding?** A subtype implements/replaces inherited behavior; `TransactionServiceImpl.create` implements the interface contract.
12. **Overloading versus overriding?** Overloading is selected from parameter signatures; overriding dispatches to the runtime object's implementation.
13. **Abstract class versus interface?** An abstract class can share instance state/implementation; an interface mainly defines a contract. BaseEntity and TransactionService serve those separate purposes.
14. **What is a checked exception?** An exception callers must catch or declare, such as IOException during CSV writing.
15. **What is an unchecked exception?** A RuntimeException subtype; domain validation errors are unchecked and handled centrally.
16. **Why create custom exceptions?** To distinguish domain failures such as duplicate budgets from unrelated runtime failures.
17. **What is the Collections Framework?** Standard generic structures and algorithms such as List, Set and Map.
18. **List versus Set?** List preserves a sequence and allows duplicates; Set models unique values, such as valid sort names.
19. **What is a Map?** Key/value association; budget calculations map category IDs to aggregate spending.
20. **What are generics?** Parameterized types for reusable type-safe code; `PageView<TransactionView>` prevents mixing unrelated DTO types.
21. **What is a Stream?** A pipeline of transformations/reductions over data, distinct from the underlying collection.
22. **What is a lambda?** An inline implementation of a functional interface, such as `b -> b.amount()`.
23. **What is a method reference?** A shorthand for existing callable behavior, such as `BigDecimal::add`.
24. **What is Optional?** A container expressing presence/absence; a scoped repository lookup may return empty and trigger a 404 exception.
25. **What is an enum?** A finite named set of values; payment method and transaction/category types are enums.
26. **Why BigDecimal instead of double for money?** Decimal arithmetic preserves cents predictably; double uses binary fractions and can introduce rounding artifacts.
27. **What is a record?** A concise immutable data carrier with generated constructor, accessors, equality and hash code; requests/responses use records.
28. **What is immutability?** State cannot change after construction; immutable DTOs simplify reasoning at API boundaries.
29. **How does Java file/I/O handling appear here?** CSV export writes escaped UTF-8 text through Writer/OutputStream to a real downloaded file.
30. **Where is background processing useful here?** Scheduled deletion of expired sessions; financial CRUD itself remains synchronous and transactional.

## B. Spring Boot, JPA and security — 30 questions

1. **What is Spring Boot?** A framework that simplifies Spring setup using starters, auto-configuration and an embedded server.
2. **Why Spring Boot for this project?** It provides integrated web, validation, security, persistence and testing support with a manageable project structure.
3. **What is dependency injection?** The container supplies required collaborators rather than classes constructing dependencies internally.
4. **Why constructor injection?** Required dependencies are explicit, easily mocked, and available as final fields.
5. **What is a Spring bean?** An object created and managed by the Spring container.
6. **What does @SpringBootApplication do?** Combines configuration, auto-configuration and component scanning from the application package.
7. **What is @RestController?** A controller whose return values are serialized directly into response bodies.
8. **Why a service layer?** It centralizes business rules, ownership checks and transaction boundaries independently of HTTP handling.
9. **What is a repository?** A persistence abstraction; Spring Data implements common CRUD/query behavior from interface declarations.
10. **What is a DTO?** A boundary-specific data representation that avoids exposing persistence internals and sensitive fields.
11. **What is Jakarta Bean Validation?** Declarative input constraints such as @NotBlank, @Email and @Positive.
12. **What does @Valid do?** Requests validation of the bound DTO before the controller uses it.
13. **What is @RestControllerAdvice?** Cross-controller handling for consistent exceptions and error responses.
14. **What is JPA?** The Java/Jakarta persistence specification for ORM entities, relationships and persistence operations.
15. **What is Hibernate?** A JPA implementation that maps objects to tables, generates SQL and tracks entity changes.
16. **JPA versus JDBC?** JDBC sends SQL through database connections; JPA adds object mapping on top, typically using JDBC underneath.
17. **What is @Entity?** Declares a persistence-mapped class with an identity and table mapping.
18. **What is @MappedSuperclass?** Shares mapped attributes without requiring a separate table for the superclass.
19. **What is @ManyToOne?** Many child records reference one parent, such as transactions referencing one category.
20. **What is @OneToMany?** An inverse or owning collection relationship, such as a user's transactions.
21. **Lazy versus eager loading?** Lazy loads relationships when needed; eager loads them immediately. FinTrack favors lazy associations and targeted fetches.
22. **What is the N+1 query problem?** Loading N records then issuing one extra query for each related record; category fetches avoid it for transaction pages.
23. **What is @Transactional?** Defines a unit of work committed atomically or rolled back on qualifying failure.
24. **What is dirty checking?** Hibernate detects changes to managed entities and synchronizes them to the database at flush/commit.
25. **What is a JPA Specification?** A composable criteria predicate used for dynamic, parameterized backend filters.
26. **What is BCrypt?** A salted adaptive password hash function; verification compares a supplied password with a stored hash, not by decryption.
27. **What is JWT?** A signed token carrying claims. Its signature protects integrity; the payload is not inherently encrypted.
28. **Authentication versus authorization?** Authentication identifies the user; authorization determines which resources that user may access.
29. **Why store JWT session IDs?** A signature alone cannot immediately revoke a still-valid token; a persisted session record permits durable logout revocation.
30. **Why disable CSRF here?** Authentication is supplied explicitly in an Authorization header, not automatically sent cookies. A cookie-auth redesign would need renewed CSRF analysis.

## C. SQL and MySQL — 20 questions

1. **What is a relational database?** Data organized into tables linked through keys and constraints.
2. **What is a primary key?** A nonnull unique identifier for each row, such as the generated transaction ID.
3. **What is a foreign key?** A constraint requiring a child reference to match an existing parent key.
4. **What is normalization?** Structuring tables to reduce duplication and update anomalies; user and category data are stored separately.
5. **Why separate categories from transactions?** One category label/type can be referenced consistently without repeating its definition on every transaction.
6. **What is a UNIQUE constraint?** A database-enforced prohibition of duplicate key values, such as one email per user account.
7. **What is a composite unique key?** Uniqueness across several columns; budgets use user/category/month/year.
8. **What is an index?** A data structure that accelerates selected lookups/sorts at a cost in storage and write work.
9. **Why start indexes with user_id?** Every protected business query is scoped to a user, making that a useful leading predicate.
10. **What is a JOIN?** A query that combines related rows, such as transactions with their category names.
11. **INNER JOIN versus LEFT JOIN?** Inner join keeps matching pairs; left join also retains unmatched rows from the left side.
12. **What does GROUP BY do?** Produces groups for aggregate calculations, such as total expense per category.
13. **WHERE versus HAVING?** WHERE filters rows before grouping; HAVING filters aggregate groups afterward.
14. **Why DECIMAL(15,2)?** It stores exact decimal amounts with 13 integer and 2 fractional digits; floating types are unsuitable for canonical currency totals.
15. **DATE versus DATETIME?** DATE models calendar dates; DATETIME includes time. Transactions use dates and audit fields use microsecond timestamps.
16. **What is ACID?** Atomicity, consistency, isolation and durability describe reliable database transaction behavior.
17. **Why InnoDB?** It supports transactions, foreign keys and row-level concurrency behavior required by this application.
18. **What is a CHECK constraint?** A database rule such as amount > 0 or saved amount ≤ target, providing defense below application validation.
19. **Why a composite category/user foreign key?** It prevents a transaction or budget from referencing a category owned by another user even if service code is bypassed.
20. **Why not store calculated balance in users?** It would duplicate derived state and could become stale; aggregate queries compute it from authoritative transactions.

## D. Full Stack Development — 20 questions

1. **What is semantic HTML?** Elements conveying content roles, such as main, nav, form, label and table headings.
2. **What is the CSS box model?** Content, padding, border and margin determine element layout; border-box includes padding/border in declared width.
3. **Flexbox versus Grid?** Flexbox aligns primarily along one axis; Grid defines rows and columns together.
4. **What is responsive design?** Adapting layout and interaction to viewport sizes using fluid dimensions, wrapping and media queries.
5. **Why Bootstrap?** Consistent base components and utilities reduce repetitive styling while custom CSS establishes the finance design.
6. **What is the DOM?** The browser's object representation of the document, manipulated by JavaScript to render live data.
7. **What is event delegation?** One ancestor handles bubbling events from many dynamic children, such as edit/delete buttons in transaction rows.
8. **What is Fetch API?** Browser HTTP functionality returning a Promise for a Response; the app wraps it centrally.
9. **What are async and await?** Syntax for writing Promise-based asynchronous behavior in a sequential-looking style.
10. **What is JSON?** A language-independent textual data format used for request and response DTOs.
11. **What is REST?** Resource-oriented HTTP interaction using methods, representations and meaningful status codes.
12. **What is CRUD?** Create, read, update and delete; implemented for transactions, categories, budgets and goals.
13. **PUT versus PATCH?** PUT replaces the editable representation; PATCH makes a partial modification. FinTrack uses PUT DTOs for full editable updates.
14. **What is CORS?** Browser-enforced cross-origin access rules negotiated with server headers; it does not replace authentication.
15. **Why validate on both client and server?** Client validation improves feedback; server validation protects integrity when clients bypass the UI.
16. **What is XSS?** Execution of attacker-controlled script in a page; escaping user values and using textContent prevents HTML interpretation here.
17. **localStorage versus sessionStorage?** Local storage persists across browser sessions; session storage is scoped to a browsing tab/session. FinTrack stores only theme and token respectively.
18. **How does CSV downloading work?** Fetch receives a real blob, creates a temporary object URL and clicks a download link before revoking the URL.
19. **How does Chart.js get its data?** Page modules fetch backend aggregates, then supply labels and datasets to the shared chart helper.
20. **How is accessibility supported?** Labeled inputs/buttons, semantic landmarks, focus indicators, native dialogs, skip links, inert closed navigation and reduced-motion support.

## E. Project-specific — 20 questions

1. **What problem does FinTrack solve?** It brings personal cash flow, spending plans and savings progress into one account-specific workspace.
2. **How is balance calculated?** Sum of income amounts minus sum of expense amounts, through today.
3. **How is savings rate calculated?** Net savings divided by total income ×100; the result is 0 when income is zero.
4. **Can the savings rate be negative?** Yes. Expenses exceeding income produce negative savings and a negative rate.
5. **How is monthly budget spending calculated?** Sum of this user's expense transactions in the budget category and exact calendar month.
6. **What are budget warning thresholds?** Below 75 normal; 75 to under 90 warning; 90 through 100 critical; above 100 exceeded.
7. **Why can remaining budget be negative?** A negative value explicitly shows overspending instead of hiding it with a zero clamp.
8. **How is goal progress calculated?** Saved amount / target amount ×100; remaining is target − saved; deadline difference gives signed days remaining.
9. **Does updating a goal affect transaction balance?** No. Goals represent manual allocation progress, preventing double counting of cash flow.
10. **Why reject saved amount greater than target?** It keeps progress within the intended allocation model; increase the target if a larger goal is desired.
11. **What does BOTH category type mean?** The category can classify income or expense, and can support a monthly expense budget.
12. **Why cannot an in-use category be deleted?** Financial history and budget references must remain valid; the user must reassign/remove dependencies first.
13. **How are users isolated?** JWT identity supplies the owner, scoped repository queries enforce it, and composite MySQL keys protect category ownership.
14. **Why 404 instead of revealing another user's record?** It avoids confirming that the inaccessible resource exists while denying access.
15. **How is transaction history kept efficient?** Backend filtering, allowlisted sorting, page-size caps and suitable database indexes.
16. **Why use aggregate SQL for the dashboard?** It transfers compact grouped results instead of repeatedly downloading every transaction into application memory.
17. **What exactly is average transaction in a report?** Sum of all income and expense magnitudes divided by count; it is not average net balance or only average expense.
18. **Does export include only the visible page?** No. It iterates all matching transactions in bounded 100-row batches and writes real CSV bytes.
19. **What happens on logout?** The persisted session ID is deleted and the tab token removed; reuse of that signed token is rejected.
20. **How would you extend the project next?** Add verified email/password reset, recurring transactions, migrations, backups and account/multi-currency support without changing the basic layered design.
