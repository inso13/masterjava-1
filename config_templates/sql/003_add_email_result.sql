
CREATE TABLE email_result (
  id          INTEGER PRIMARY KEY DEFAULT nextval('common_seq'),
  result TEXT NOT NULL,
  datetime TIMESTAMP NOT NULL
);
