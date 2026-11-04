# Complete file inventory

All paths are relative to `FinTrack/`. Checked entries are actual project files. Build output and `.git/` internals are not source files.

## Root and database

- [x] `.gitignore`
- [x] `README.md`
- [x] `backend/pom.xml`
- [x] `backend/src/main/resources/application.properties`
- [x] `backend/src/main/resources/application.properties.example`
- [x] `database/schema.sql`
- [x] `database/seed.sql`

## Java application — 52 source files

- [x] `backend/src/main/java/com/fintrack/FinTrackApplication.java`
- [x] `backend/src/main/java/com/fintrack/config/DemoDataSeeder.java`
- [x] `backend/src/main/java/com/fintrack/config/SecurityConfig.java`
- [x] `backend/src/main/java/com/fintrack/controller/AuthController.java`
- [x] `backend/src/main/java/com/fintrack/controller/UserController.java`
- [x] `backend/src/main/java/com/fintrack/controller/TransactionController.java`
- [x] `backend/src/main/java/com/fintrack/controller/CategoryController.java`
- [x] `backend/src/main/java/com/fintrack/controller/BudgetController.java`
- [x] `backend/src/main/java/com/fintrack/controller/GoalController.java`
- [x] `backend/src/main/java/com/fintrack/controller/DashboardController.java`
- [x] `backend/src/main/java/com/fintrack/controller/ReportController.java`
- [x] `backend/src/main/java/com/fintrack/dto/Requests.java`
- [x] `backend/src/main/java/com/fintrack/dto/Views.java`
- [x] `backend/src/main/java/com/fintrack/dto/TransactionFilter.java`
- [x] `backend/src/main/java/com/fintrack/dto/TransactionQuery.java`
- [x] `backend/src/main/java/com/fintrack/entity/BaseEntity.java`
- [x] `backend/src/main/java/com/fintrack/entity/User.java`
- [x] `backend/src/main/java/com/fintrack/entity/Category.java`
- [x] `backend/src/main/java/com/fintrack/entity/Transaction.java`
- [x] `backend/src/main/java/com/fintrack/entity/Budget.java`
- [x] `backend/src/main/java/com/fintrack/entity/FinancialGoal.java`
- [x] `backend/src/main/java/com/fintrack/entity/AuthSession.java`
- [x] `backend/src/main/java/com/fintrack/entity/TransactionType.java`
- [x] `backend/src/main/java/com/fintrack/entity/CategoryType.java`
- [x] `backend/src/main/java/com/fintrack/entity/PaymentMethod.java`
- [x] `backend/src/main/java/com/fintrack/repository/UserRepository.java`
- [x] `backend/src/main/java/com/fintrack/repository/CategoryRepository.java`
- [x] `backend/src/main/java/com/fintrack/repository/TransactionRepository.java`
- [x] `backend/src/main/java/com/fintrack/repository/TransactionSpecifications.java`
- [x] `backend/src/main/java/com/fintrack/repository/BudgetRepository.java`
- [x] `backend/src/main/java/com/fintrack/repository/GoalRepository.java`
- [x] `backend/src/main/java/com/fintrack/repository/AuthSessionRepository.java`
- [x] `backend/src/main/java/com/fintrack/service/UserService.java`
- [x] `backend/src/main/java/com/fintrack/service/AuthService.java`
- [x] `backend/src/main/java/com/fintrack/service/CategoryService.java`
- [x] `backend/src/main/java/com/fintrack/service/TransactionService.java`
- [x] `backend/src/main/java/com/fintrack/service/impl/TransactionServiceImpl.java`
- [x] `backend/src/main/java/com/fintrack/service/BudgetService.java`
- [x] `backend/src/main/java/com/fintrack/service/GoalService.java`
- [x] `backend/src/main/java/com/fintrack/service/AnalyticsService.java`
- [x] `backend/src/main/java/com/fintrack/service/CsvExportService.java`
- [x] `backend/src/main/java/com/fintrack/security/CurrentUser.java`
- [x] `backend/src/main/java/com/fintrack/security/JwtService.java`
- [x] `backend/src/main/java/com/fintrack/security/JwtAuthenticationFilter.java`
- [x] `backend/src/main/java/com/fintrack/exception/ApiError.java`
- [x] `backend/src/main/java/com/fintrack/exception/BadRequestException.java`
- [x] `backend/src/main/java/com/fintrack/exception/ConflictException.java`
- [x] `backend/src/main/java/com/fintrack/exception/ResourceNotFoundException.java`
- [x] `backend/src/main/java/com/fintrack/exception/GlobalExceptionHandler.java`
- [x] `backend/src/main/java/com/fintrack/mapper/FinanceMapper.java`
- [x] `backend/src/main/java/com/fintrack/util/DateRanges.java`
- [x] `backend/src/main/java/com/fintrack/util/FinancialMath.java`

## Tests and API collection

- [x] `backend/src/test/resources/application-test.properties`
- [x] `backend/src/test/java/com/fintrack/ApiIntegrationTest.java`
- [x] `backend/src/test/java/com/fintrack/service/AuthServiceTest.java`
- [x] `backend/src/test/java/com/fintrack/service/TransactionServiceTest.java`
- [x] `backend/src/test/java/com/fintrack/service/FinancialCalculationsTest.java`
- [x] `backend/src/test/java/com/fintrack/service/AnalyticsServiceTest.java`
- [x] `postman/FinTrack.postman_collection.json`

## Frontend — nine pages

- [x] `frontend/index.html`
- [x] `frontend/login.html`
- [x] `frontend/register.html`
- [x] `frontend/dashboard.html`
- [x] `frontend/transactions.html`
- [x] `frontend/budgets.html`
- [x] `frontend/goals.html`
- [x] `frontend/reports.html`
- [x] `frontend/profile.html`
- [x] `frontend/css/style.css`
- [x] `frontend/css/dashboard.css`
- [x] `frontend/css/design.css`
- [x] `frontend/css/responsive.css`
- [x] `frontend/js/config.js`
- [x] `frontend/js/theme.js`
- [x] `frontend/js/experience.js`
- [x] `frontend/js/api.js`
- [x] `frontend/js/ui.js`
- [x] `frontend/js/charts.js`
- [x] `frontend/js/auth.js`
- [x] `frontend/js/dashboard.js`
- [x] `frontend/js/transactions.js`
- [x] `frontend/js/budgets.js`
- [x] `frontend/js/goals.js`
- [x] `frontend/js/reports.js`
- [x] `frontend/js/profile.js`
- [x] `frontend/assets/README.md`

## Documentation

- [x] `docs/architecture.md`
- [x] `docs/api-documentation.md`
- [x] `docs/database-design.md`
- [x] `docs/java-concepts.md`
- [x] `docs/fsd-concepts.md`
- [x] `docs/viva-questions.md`
- [x] `docs/presentation.md`
- [x] `docs/testing.md`
- [x] `docs/verification.md`
- [x] `docs/file-inventory.md`

## Real application screenshots

- [x] `screenshots/README.md`
- [x] `screenshots/dashboard-light.png`
- [x] `screenshots/dashboard-dark.png`
- [x] `screenshots/transactions.png`
- [x] `screenshots/budgets.png`
- [x] `screenshots/goals.png`
- [x] `screenshots/reports.png`
- [x] `screenshots/mobile-360.png`
- [x] `screenshots/landing.png`
- [x] `screenshots/landing-mobile.png`
- [x] `screenshots/login.png`

## Generated locally

- [x] `backend/target/fintrack-1.0.0.jar` — executable build artifact, Git-ignored
- [x] `backend/target/surefire-reports/` — test reports, Git-ignored
- [x] `.git/` — initialized repository, no commit/remote publication

## Intentional choices / omissions

- No `application.yml.example`: the project uses one configuration format consistently.
- No populated local `backend/application.properties`: copy the example and supply your own local credentials; actual secrets are not shipped.
- Request/response records are grouped in `Requests.java` and `Views.java`; each is a real, compiled DTO type, not a missing separate file.
- One `ResourceNotFoundException` parameterized by resource name replaces redundant per-resource exception subclasses.
- No optional PDF export or financial-health score. Required CSV/reporting and all core domain flows are implemented.
- No application Node.js dependency, frontend framework, fake database, or unimplemented required feature. Browser/Postman verification tools were external development tools.
