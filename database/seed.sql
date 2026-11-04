-- Optional MANUAL seed, instead of DEMO_ENABLED. First register roykaustav382@gmail.com
-- through the UI so the application creates a BCrypt password and categories.
-- This script is idempotent and contains no passwords. The Java seeder provides
-- the richer six-month dataset; do not use both on the same account.
USE fintrack;
SET @uid = (SELECT id FROM users WHERE email = 'roykaustav382@gmail.com');
SET @first = DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE)-1 DAY);

INSERT INTO transactions (user_id, category_id, amount, type, description, payment_method, transaction_date, created_at, updated_at)
SELECT @uid, c.id, s.amount, s.type, s.description, 'BANK_TRANSFER', @first, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM categories c JOIN (
  SELECT 'Salary' category, 55000 amount, 'INCOME' type, 'SQL demo salary' description
  UNION ALL SELECT 'Freelance', 8000, 'INCOME', 'SQL demo freelance project'
  UNION ALL SELECT 'Food', 3800, 'EXPENSE', 'SQL demo groceries'
  UNION ALL SELECT 'Transport', 1850, 'EXPENSE', 'SQL demo commute'
  UNION ALL SELECT 'Shopping', 4200, 'EXPENSE', 'SQL demo home essentials'
) s ON c.name = s.category
WHERE c.user_id = @uid AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.user_id = @uid AND t.description = s.description);

INSERT INTO budgets (user_id, category_id, amount, budget_month, budget_year, created_at, updated_at)
SELECT @uid, c.id, 5000, MONTH(CURRENT_DATE), YEAR(CURRENT_DATE), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM categories c WHERE c.user_id = @uid AND c.name = 'Food'
AND NOT EXISTS (SELECT 1 FROM budgets b WHERE b.user_id = @uid AND b.category_id = c.id AND b.budget_month = MONTH(CURRENT_DATE) AND b.budget_year = YEAR(CURRENT_DATE));

INSERT INTO financial_goals (user_id, goal_name, target_amount, saved_amount, deadline, created_at, updated_at)
SELECT @uid, 'New laptop', 80000, 45000, DATE_ADD(CURRENT_DATE, INTERVAL 6 MONTH), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
WHERE @uid IS NOT NULL AND NOT EXISTS (SELECT 1 FROM financial_goals WHERE user_id = @uid AND goal_name = 'New laptop');

SELECT CASE WHEN @uid IS NULL THEN 'Register roykaustav382@gmail.com first; no data was inserted.' ELSE 'Manual demo data ready.' END AS seed_status;
