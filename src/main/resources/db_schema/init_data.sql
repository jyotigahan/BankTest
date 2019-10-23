INSERT INTO transaction_status (id, name)
VALUES
       (1, 'Planned'),
       (2, 'Processing'),
       (3, 'Failed'),
       (4, 'Succeed');

INSERT INTO bank_account (owner_name, balance, blocked_amount)
VALUES
  ('Jyoti', 1000.5, 0),
  ('Ranjan', 1000.5, 0),
  ('Gahan', 1000.5, 0);