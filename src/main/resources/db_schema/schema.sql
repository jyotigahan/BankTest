CREATE TABLE IF NOT EXISTS bank_account (
  id IDENTITY,
  owner_name VARCHAR(256) NOT NULL,
  balance DECIMAL(19,4) NOT NULL,
  blocked_amount DECIMAL(19,4) NOT NULL,
);

CREATE TABLE IF NOT EXISTS transaction_status (
  id INT PRIMARY KEY,
  name VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS transaction (
  id IDENTITY,
  from_account_id BIGINT NOT NULL,
  to_account_id BIGINT NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  creation_date TIMESTAMP NOT NULL,
  update_date TIMESTAMP,
  status_id INT NOT NULL,
  failMessage VARCHAR(4000),

  FOREIGN KEY(from_account_id) REFERENCES bank_account(id),
  FOREIGN KEY(to_account_id) REFERENCES bank_account(id),
  FOREIGN KEY(status_id) REFERENCES transaction_status(id)
)