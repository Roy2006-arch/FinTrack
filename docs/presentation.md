# HOW TO PRESENT FINTRACK

## Opening — 30 seconds

“FinTrack is a multi-user personal finance management system. It connects a plain HTML/CSS/JavaScript frontend to a secured Spring Boot API and a MySQL relational database. One codebase demonstrates Java/OOP architecture and complete full-stack behavior.”

## A. Java evaluation — 8–10 minutes

1. **OOP structure:** show packages, `TransactionService` and its implementation; explain abstraction and runtime polymorphism.
2. **Entities:** show private fields/accessors, enums, `BaseEntity` inheritance and lazy relationships.
3. **Service interface:** follow transaction creation from controller through the contract into implementation.
4. **Exceptions:** submit an invalid amount and a foreign ID; show central error JSON and custom exceptions.
5. **Collections:** show List response, Set sort allowlist, Map category totals.
6. **Streams:** show budget total reduction and DTO mapping; distinguish SQL aggregation from in-memory list processing.
7. **REST:** inspect POST/GET/PUT/DELETE and 201/200/204/400/404/409 statuses.
8. **Spring Boot:** explain application entry point, starters, constructor DI and embedded Tomcat.
9. **JPA/Hibernate:** show repository methods, Specifications, generated SQL/aggregate query, transaction boundary.
10. **Security:** show BCrypt hash in DB, JWT filter, scoped user queries and durable session revocation.
11. Run `mvn test`; explain one Mockito unit test and one full MockMvc integration test.

## B. FSD evaluation — 8–10 minutes

1. Login with the populated demo account; explain Fetch and token handling.
2. Show dashboard cards and API-driven charts.
3. Add an expense to Food in the current month.
4. Edit its amount; show the changed value persisted after reload.
5. Delete it, first cancelling and then confirming the dialog.
6. Search/filter by description, type, category, date and payment; change sorting and paginate.
7. Show budgets at normal, warning, critical and exceeded levels; connect a new expense to updated budget usage.
8. Create a goal and edit saved amount; explain that manual savings allocation does not insert a cash-flow transaction.
9. Show monthly report, custom dates, category doughnut and spending line; download actual CSV files.
10. Switch light/dark mode and emulate 360px mobile navigation/table scrolling.
11. Edit profile and create a personal category; sign out and demonstrate protected-route behavior.

## C. Database evaluation — 5 minutes

- Draw the ER diagram: one user to many categories/transactions/budgets/goals/sessions; category to many transactions/budgets.
- Explain primary keys, foreign keys, uniqueness, decimal money and date/timestamp types.
- Show `users.password` is BCrypt and responses do not expose it.
- Explain normalized category storage and why derived totals are calculated instead of persisted.
- Run a scoped SUM/GROUP BY query and explain composite indexes.
- Show duplicate budget rejection and referenced-category deletion protection.
- Explain composite category/user foreign keys as database-level tenant integrity.

Example read-only demonstration queries:

```sql
SELECT id, name, email, LEFT(password, 7) AS hash_prefix FROM users;
SELECT type, SUM(amount) AS total, COUNT(*) AS entries
FROM transactions WHERE user_id = 1 GROUP BY type;
SELECT c.name, SUM(t.amount) AS expense_total
FROM transactions t JOIN categories c ON c.id = t.category_id
WHERE t.user_id = 1 AND t.type = 'EXPENSE'
GROUP BY c.id, c.name ORDER BY expense_total DESC;
```

Replace user ID with the demo user's actual ID.

## D. Project viva — 5 minutes

Be ready to explain:

- Why a layered monolith is sufficient and easier to evaluate than microservices.
- Why JWT still uses a session table: immediate, persistent logout revocation.
- Why BigDecimal is used for backend money rather than double.
- Why foreign resource IDs return 404, and why client-supplied owner IDs are ignored/rejected.
- Why goals are not automatically deducted from balances.
- Why a used category's type cannot change and how changing its name affects history labels.
- How budget thresholds and zero-income calculations work.
- How export differs from exporting just the current page.
- Why aggregate SQL is preferable to fetching all transactions for every chart.
- Which optional features were intentionally omitted: PDF/health scoring/bank integration.

Use the [120 answered questions](viva-questions.md) for preparation. End with a short live change: add an expense, inspect MySQL, return to the dashboard and budget page, and export the result.
