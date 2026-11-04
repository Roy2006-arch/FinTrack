# REST API documentation

Base URL: `http://localhost:8080/api`. Content type: `application/json`. Dates: ISO `yyyy-MM-dd`; timestamps: ISO UTC. Money is a JSON number in responses and may be a decimal number/string in requests, with up to 13 integer digits and 2 decimal places. Currency: INR.

## Shared headers, authentication and errors

Every protected endpoint below requires:

```http
Authorization: Bearer <token from register/login>
Accept: application/json
Content-Type: application/json
```

`Content-Type` is required only when sending a JSON body. Downloads accept `text/csv`. Registration/login are public and need no Authorization header. GET/DELETE/logout have **no request body**. Client-supplied owner/user IDs are never accepted in business DTOs. Unknown JSON properties are rejected.

All endpoints share this error schema:

```json
{
  "timestamp": "2026-10-03T12:00:00Z",
  "status": 404,
  "message": "Transaction not found",
  "path": "/api/transactions/12",
  "errors": {}
}
```

Validation errors are 400 and add field details, for example `"errors":{"amount":"must be greater than 0"}`. All protected routes can return 401 (missing, expired, tampered or revoked token). 404 is used for missing **and foreign-owned** resources. 409 means duplicate or currently referenced resource. Unexpected errors return generic 500 JSON without a stack trace. A disallowed CORS origin returns 403 from Spring's CORS filter (plain `Invalid CORS request`); security access denial otherwise uses JSON. Wrong methods return 405. Table error columns list additional domain cases; 400 malformed values and 500 remain possible for every route.

## Authentication

| Method | URL | Purpose / request body | Success | Additional errors |
|---|---|---|---|---|
| POST | `/auth/register` | Create account; `Register` below | 201 `AuthView`; default categories created | 400 invalid/weak fields; 409 email exists |
| POST | `/auth/login` | Verify credentials; `Login` below | 200 `AuthView` | 401 wrong email/password |
| POST | `/auth/logout` | Revoke current JWT session; no body; protected | 204 empty | 401 invalid/revoked session |

Register:

```json
{"name":"Kaustav Roy","email":"roykaustav382@gmail.com","password":"DemoPass123!"}
```

Login:

```json
{"email":"roykaustav382@gmail.com","password":"DemoPass123!"}
```

Password policy: 10–72 printable non-space ASCII characters with uppercase, lowercase and a digit. The sample password is public demo data, not a production secret. Emails are normalized to lowercase; names are trimmed.

`AuthView` contains `token` (signed bearer JWT), `expiresAt` (timestamp) and `user` (`UserView`). Expiry defaults to 120 minutes. A real response supplies the actual token; copy it into the header rather than using a token-shaped placeholder. Login creates a new independent session. Logout revokes only that session. There is no refresh endpoint.

## Users

| Method | URL | Purpose / body | Success | Additional errors |
|---|---|---|---|---|
| GET | `/users/me` | Current profile; no body | 200 `UserView` | 404 user no longer exists |
| PUT | `/users/me` | Replace editable profile fields; `Profile` | 200 `UserView` | 400 invalid phone/email/name; 409 email taken |

Profile:

```json
{"name":"Kaustav Roy","email":"roykaustav382@gmail.com","phone":"+91 90000 00000"}
```

Phone can be null/empty, or up to 20 digits, spaces, `+`, `-`, `(`, `)`. Response:

```json
{"id":1,"name":"Kaustav Roy","email":"roykaustav382@gmail.com","phone":null,"createdAt":"2026-10-03T12:00:00Z"}
```

No password/hash/session data is included in `UserView`.

## Categories

| Method | URL | Purpose / body | Success | Additional errors |
|---|---|---|---|---|
| GET | `/categories` | All current-user categories ordered by name; no body | 200 array of `CategoryView` | — |
| POST | `/categories` | Create; `CategoryInput` | 201 `CategoryView` | 409 duplicate name |
| PUT | `/categories/{id}` | Rename/retype; `CategoryInput` | 200 `CategoryView` | 404 missing; 409 duplicate name or retyping category in use |
| DELETE | `/categories/{id}` | Delete unused category; no body | 204 empty | 404 missing; 409 referenced by transaction/budget |

Input: `{"name":"Education","type":"EXPENSE"}`. Name: nonblank, maximum 60 characters; type: `INCOME`, `EXPENSE`, `BOTH`.

`CategoryView`: `{"id":8,"name":"Education","type":"EXPENSE"}`. A `BOTH` category accepts either transaction type and can have an expense budget. Used categories can be renamed but not retyped.

## Transactions

| Method | URL | Purpose / body | Success | Additional errors |
|---|---|---|---|---|
| GET | `/transactions` | Search/filter/sort/paginate; no body | 200 `PageView<TransactionView>` | 400 invalid page/sort/date range |
| GET | `/transactions/{id}` | One owned transaction; no body | 200 `TransactionView` | 404 missing/foreign |
| POST | `/transactions` | Create; `TransactionInput` | 201 `TransactionView` | 400 incompatible category type; 404 category missing/foreign |
| PUT | `/transactions/{id}` | Replace editable values; `TransactionInput` | 200 `TransactionView` | 404 transaction/category missing; 400 incompatible type |
| DELETE | `/transactions/{id}` | Delete; no body | 204 empty | 404 missing/foreign |
| GET | `/transactions/export` | Export **all matches**, same filters/sort; no body | 200 UTF-8 CSV attachment | 400 invalid filters |

TransactionInput:

```json
{
  "amount": 500,
  "type": "EXPENSE",
  "categoryId": 3,
  "description": "Restaurant",
  "paymentMethod": "UPI",
  "transactionDate": "2026-10-03"
}
```

Category IDs must come from your own `/categories` response. Type: `INCOME` or `EXPENSE`. Payment: `CASH`, `UPI`, `CARD`, `BANK_TRANSFER`, `OTHER`. Amount > 0; description required but may be empty, maximum 500; date required and not in future.

TransactionView:

```json
{"id":12,"amount":500.00,"type":"EXPENSE","categoryId":3,"categoryName":"Food","description":"Restaurant","paymentMethod":"UPI","transactionDate":"2026-10-03"}
```

PageView:

```json
{"content":[{"id":12,"amount":500.00,"type":"EXPENSE","categoryId":3,"categoryName":"Food","description":"Restaurant","paymentMethod":"UPI","transactionDate":"2026-10-03"}],"page":0,"size":15,"totalElements":1,"totalPages":1}
```

Query parameters (all optional):

| Parameter | Meaning / default |
|---|---|
| search | Case-insensitive description or category substring, ≤100 chars; `%` and `_` treated literally |
| categoryId | Owned category ID; foreign IDs produce an empty list |
| type | INCOME or EXPENSE |
| paymentMethod | Payment enum |
| start / end | Inclusive transaction-date bounds, either optional; start ≤ end |
| sort | `transactionDate` (default) or `amount` |
| direction | `desc` (default) or `asc` |
| page | Zero-based; default 0; nonnegative |
| size | Default 15; from 1 to 100 |

Example: `/transactions?search=restaurant&type=EXPENSE&paymentMethod=UPI&start=2026-10-01&end=2026-10-31&sort=amount&direction=desc&page=0&size=15`.

Export ignores the chosen page for output and iterates all matches in 100-row batches. CSV columns: ID, Date, Type, Category, Description, Payment method, Amount (INR). The header is `Content-Disposition: attachment; filename=fintrack-transactions.csv`. Formula-leading text cells are neutralized for spreadsheet safety.

## Budgets

| Method | URL | Purpose / body | Success | Additional errors |
|---|---|---|---|---|
| GET | `/budgets?month=10&year=2026` | Month's budgets; no body; defaults current month/year | 200 array of `BudgetView` | 400 invalid month/year |
| GET | `/budgets/{id}` | Owned budget with computed usage; no body | 200 `BudgetView` | 404 missing/foreign |
| POST | `/budgets` | Create monthly limit; `BudgetInput` | 201 `BudgetView` | 409 duplicate category/month; 400 income-only category; 404 category |
| PUT | `/budgets/{id}` | Replace limit/category/month; `BudgetInput` | 200 `BudgetView` | 404 missing/category; 409 duplicate period; 400 type |
| DELETE | `/budgets/{id}` | Delete budget only; no body | 204 empty | 404 missing/foreign |

Input: `{"categoryId":3,"amount":5000,"month":10,"year":2026}`. Amount positive; month 1–12; year 2000–2100. Budget deletion does not delete transactions.

```json
{"id":1,"categoryId":3,"categoryName":"Food","amount":5000.00,"month":10,"year":2026,"spent":3800.00,"remaining":1200.00,"usagePercentage":76.00,"warningLevel":"WARNING"}
```

Spending uses expense transactions in the exact category and calendar month. Remaining = limit − spending (may be negative). Usage = spending/limit ×100. `<75 NORMAL`; `75–<90 WARNING`; `90–100 CRITICAL`; `>100 EXCEEDED`.

## Financial goals

| Method | URL | Purpose / body | Success | Additional errors |
|---|---|---|---|---|
| GET | `/goals` | Owned goals ordered by deadline; no body | 200 array of `GoalView` | — |
| GET | `/goals/{id}` | One goal; no body | 200 `GoalView` | 404 missing/foreign |
| POST | `/goals` | Create; `GoalInput` | 201 `GoalView` | 400 saved > target or deadline year invalid |
| PUT | `/goals/{id}` | Replace values/update savings; `GoalInput` | 200 `GoalView` | 404 missing; 400 invalid values |
| DELETE | `/goals/{id}` | Delete goal; no body | 204 empty | 404 missing/foreign |

Input: `{"goalName":"Laptop","targetAmount":80000,"savedAmount":45000,"deadline":"2027-04-03"}`. Name nonblank ≤100; target positive; 0 ≤ saved ≤ target; deadline year 2000–2100; past dates are allowed.

```json
{"id":1,"goalName":"Laptop","targetAmount":80000.00,"savedAmount":45000.00,"deadline":"2027-04-03","progressPercentage":56.25,"remainingAmount":35000.00,"daysRemaining":182}
```

`daysRemaining` depends on the current server date and may be negative. Goals are manual allocations, not bank balances; editing one does not insert transactions.

## Dashboard

All routes are protected GETs with no request body.

| URL | Purpose and query | 200 response | Additional errors |
|---|---|---|---|
| `/dashboard/summary` | Lifetime summary plus current-month budget/spending/goal snapshots | `Summary` below | — |
| `/dashboard/monthly?year=2026` | 12 zero-filled calendar months; year defaults current | array of `MonthlyTotal` | 400 year outside 2000–2100 |
| `/dashboard/category-expenses?start=2026-10-01&end=2026-10-31` | Expense totals; omitted bounds default current month | array of `CategoryTotal` | 400 reversed range |
| `/dashboard/recent-transactions` | Latest five transactions, date then ID descending | array of `TransactionView` | — |

`MonthlyTotal`: `{"month":"2026-10","income":63000.00,"expense":20000.00}`.

`CategoryTotal`: `{"categoryId":3,"categoryName":"Food","total":3800.00}`.

Summary fields:

| Field | Meaning |
|---|---|
| totalIncome / totalExpenses | Lifetime sums through today |
| balance / savings | Income − expenses |
| savingsRate | Savings / income ×100; 0 if no income |
| currentMonthSpending | All current-month expenses |
| transactionCount | Lifetime transaction count |
| budgetAmount / budgetSpent | Current-month allocated limits and spending within budgeted categories |
| budgetUsage | budgetSpent / budgetAmount ×100; 0 without budgets |
| budgets | Array of `BudgetView` for current month |
| goals | Array of `GoalView` |

## Reports

All routes are protected GETs with no request body.

| URL | Purpose / required query | Success | Additional errors |
|---|---|---|---|
| `/reports/monthly?month=10&year=2026` | Calendar month | 200 `Report` | 400 invalid month/year |
| `/reports/category?start=2026-10-01&end=2026-10-31` | Expense totals by category | 200 array of `CategoryTotal` | 400 missing/reversed dates |
| `/reports/date-range?start=2026-10-01&end=2026-10-31` | Inclusive custom period | 200 `Report` | 400 missing/reversed dates |
| `/reports/export?start=2026-10-01&end=2026-10-31` | Real report summary/category CSV | 200 `text/csv` attachment `fintrack-report.csv` | 400 missing/reversed dates |

Report:

```json
{
  "start":"2026-10-01",
  "end":"2026-10-31",
  "totalIncome":10000.00,
  "totalExpense":2500.00,
  "netBalance":7500.00,
  "transactionCount":3,
  "highestExpense":2000.00,
  "averageTransaction":4166.67,
  "categories":[{"categoryId":3,"categoryName":"Food","total":2500.00}]
}
```

Average uses the **unsigned amounts of income and expenses**, not the net balance. Empty reports return zero numeric totals and an empty category array. Highest expense is zero with no expenses. Report CSV includes dates, income, expense, balance, count, highest expense, average transaction and category totals. Use transaction export with the same bounds for individual rows.

## Postman

Import the collection in `postman/`. Set `baseUrl` and a valid `password`. Registration generates a unique fake email. Tests capture token, categoryId, transactionId, budgetId and goalId; subsequent requests reuse them. Collection-level assertions verify each expected happy-path request is 2xx. Destructive cleanup runs at the end, followed by logout. Unit/integration tests cover rejected requests independently.
