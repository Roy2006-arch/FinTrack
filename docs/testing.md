# Testing guide

## Automated backend suite

From `backend`:

```powershell
mvn test
mvn verify
```

The test profile is isolated, uses H2 in MySQL mode and creates/drops its own schema. H2 is a test-scoped dependency and cannot become the production database accidentally. Test JWT signing material is visibly test-only.

| Test class | Cases | Coverage |
|---|---:|---|
| AuthServiceTest | 5 | BCrypt, normalization, duplicate registration, valid/invalid login, revocation |
| TransactionServiceTest | 4 | Owner/category creation, update, deletion, foreign/missing resources |
| FinancialCalculationsTest | 11 | Seven budget boundaries, actual usage, goal progress/overdue, zero/negative savings, CSV quoting/formula protection |
| AnalyticsServiceTest | 3 | Aggregate dashboard, zero-report state, twelve-month zero fill |
| ApiIntegrationTest | 7 | Full security/HTTP/JPA paths, CRUD/filter/report/export, ownership, validation, budgets/goals, category constraints, logout, CORS |
| **Total** | **30** | JUnit 5 + Mockito + Spring MockMvc |

Surefire reports are under `backend/target/surefire-reports/`. The final jar is `backend/target/fintrack-1.0.0.jar`.

## Run integration tests on a dedicated MySQL database

Create an **empty test-only database** and user with DDL privileges for that database. The test profile uses `create-drop`, so do not point it at a database containing records you want to retain.

```sql
CREATE DATABASE fintrack_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'fintrack_test'@'localhost' IDENTIFIED BY 'REPLACE_WITH_LOCAL_TEST_PASSWORD';
GRANT ALL PRIVILEGES ON fintrack_test.* TO 'fintrack_test'@'localhost';
```

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/fintrack_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:TEST_DB_USERNAME = 'fintrack_test'
$testPassword = Read-Host 'Test database password' -AsSecureString
$env:TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $testPassword).Password
mvn '-Dtest=ApiIntegrationTest' test
Remove-Item Env:TEST_DB_URL, Env:TEST_DB_USERNAME, Env:TEST_DB_PASSWORD
```

This tests Hibernate-generated mappings against MySQL. To verify the **supplied SQL schema**, also import `database/schema.sql` into a fresh development instance and run the normal app with its default `ddl-auto=validate`, then run Postman/browser checks.

## Postman end-to-end collection

1. Import `postman/FinTrack.postman_collection.json`.
2. Set collection `baseUrl` to `http://localhost:8080/api`.
3. Set collection `password` to a valid disposable test password such as `CollectionTest123!`.
4. Run all 34 requests in order. The first registration creates a unique fake email; scripts capture token and resource IDs.
5. The collection exercises all 33 unique routes, including both CSV exports, then cleans up its transaction/budget/goal/category and revokes its token.

The account and its default categories remain for inspection. The collection never operates on the seeded demo user's records. Negative scenarios belong to the integration suite rather than making the happy-path collection fail deliberately.

## Browser acceptance checklist

- Register and see a genuine empty dashboard; sign out and log in again.
- Seeded user shows six months of data, real budgets and goals.
- Add income and expense, edit amount/category/payment/date, reload and verify persistence.
- Combine search/type/category/payment/date filters, change sort order, paginate.
- Export with filters; check the CSV includes all matches, quoted descriptions and rupee amounts.
- Cancel deletion and verify the record remains; confirm and verify 204 and disappearance.
- Create/rename/delete an unused category; a used category returns a useful 409 message.
- Budget warning boundaries: 74.99, 75, 89.99, 90, 100 and >100 percent.
- Update a goal to 56.25% and then complete it; ensure balance is unchanged.
- Generate current/previous/custom reports and compare income/expense totals with SQL.
- Change profile email, then sign in with the new email.
- Toggle theme; navigate and reload; confirm preference persists and chart colors change.
- Test 1440, 1024, 768, 480 and 360 pixel widths. Page should not overflow; wide tables scroll internally.
- Navigate by keyboard; test skip link, dialog Escape/cancel, field labels and mobile navigation.
- Stop API temporarily: page error state offers retry; restart and retry successfully.
- Use a second account and request the first account's IDs: 404, no leaked data.
- Log out and reuse its old token manually: 401.

Executed results and screenshots are in [verification.md](verification.md).
