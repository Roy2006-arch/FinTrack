-- MySQL 8.0.16+ (CHECK constraints enforced). Run once before starting the API.

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL,
  password VARCHAR(60) NOT NULL,
  phone VARCHAR(20),
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  type ENUM('INCOME','EXPENSE','BOTH') NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT uq_category_name UNIQUE (user_id, name),
  CONSTRAINT uq_category_owner UNIQUE (id, user_id),
  CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  type ENUM('INCOME','EXPENSE') NOT NULL,
  description VARCHAR(500) NOT NULL,
  payment_method ENUM('CASH','UPI','CARD','BANK_TRANSFER','OTHER') NOT NULL,
  transaction_date DATE NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT ck_transaction_amount CHECK (amount > 0),
  CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_transaction_category_owner FOREIGN KEY (category_id, user_id) REFERENCES categories(id, user_id),
  INDEX idx_tx_user_date (user_id, transaction_date),
  INDEX idx_tx_user_type_date (user_id, type, transaction_date),
  INDEX idx_tx_user_category_date (user_id, category_id, transaction_date),
  INDEX idx_tx_user_amount (user_id, amount)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS budgets (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  budget_month INT NOT NULL,
  budget_year INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT ck_budget_amount CHECK (amount > 0),
  CONSTRAINT ck_budget_month CHECK (budget_month BETWEEN 1 AND 12),
  CONSTRAINT ck_budget_year CHECK (budget_year BETWEEN 2000 AND 2100),
  CONSTRAINT uq_budget_period UNIQUE (user_id, category_id, budget_month, budget_year),
  CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_budget_category_owner FOREIGN KEY (category_id, user_id) REFERENCES categories(id, user_id),
  INDEX idx_budget_user_period (user_id, budget_year, budget_month)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS financial_goals (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  goal_name VARCHAR(100) NOT NULL,
  target_amount DECIMAL(15,2) NOT NULL,
  saved_amount DECIMAL(15,2) NOT NULL,
  deadline DATE NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT ck_goal_target CHECK (target_amount > 0),
  CONSTRAINT ck_goal_saved CHECK (saved_amount >= 0 AND saved_amount <= target_amount),
  CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_goal_user_deadline (user_id, deadline)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS auth_sessions (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_session_expiry (expires_at)
) ENGINE=InnoDB;
