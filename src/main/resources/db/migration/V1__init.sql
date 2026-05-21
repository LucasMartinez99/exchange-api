CREATE TABLE IF NOT EXISTS conversion_history (
    id                BIGSERIAL PRIMARY KEY,
    source_currency   VARCHAR(10)    NOT NULL,
    target_currency   VARCHAR(10)    NOT NULL,
    amount            NUMERIC(19, 4) NOT NULL,
    exchange_rate     NUMERIC(19, 6) NOT NULL,
    converted_amount  NUMERIC(19, 6) NOT NULL,
    conversion_date   TIMESTAMP      NOT NULL,
    success           BOOLEAN        NOT NULL
);
