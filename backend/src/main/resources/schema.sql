-- FlatFlow database tables (D1 I/O spec, "Stored data file"; poster section 3).
-- Money is always whole cents. Passwords are only stored as bcrypt hashes.
-- Balances are NOT stored: they are calculated from expense + expense_share.

CREATE TABLE IF NOT EXISTS user (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    display_name  TEXT NOT NULL,
    login_name    TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    household_id  INTEGER REFERENCES household(id)
);

CREATE TABLE IF NOT EXISTS household (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    name              TEXT NOT NULL,
    organizer_user_id INTEGER NOT NULL REFERENCES user(id),
    invite_code       TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS session (
    token      TEXT PRIMARY KEY,
    user_id    INTEGER NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS chore (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id     INTEGER NOT NULL REFERENCES household(id),
    title            TEXT NOT NULL,
    description      TEXT NOT NULL DEFAULT '',
    assignee_user_id INTEGER NOT NULL REFERENCES user(id),
    due_date         TEXT NOT NULL,
    status           TEXT NOT NULL CHECK (status IN ('PENDING', 'DONE'))
);

CREATE TABLE IF NOT EXISTS expense (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id  INTEGER NOT NULL REFERENCES household(id),
    description   TEXT NOT NULL,
    date          TEXT NOT NULL,
    amount_cents  INTEGER NOT NULL CHECK (amount_cents > 0),
    payer_user_id INTEGER NOT NULL REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS expense_share (
    expense_id   INTEGER NOT NULL REFERENCES expense(id) ON DELETE CASCADE,
    user_id      INTEGER NOT NULL REFERENCES user(id),
    amount_cents INTEGER NOT NULL CHECK (amount_cents >= 0),
    PRIMARY KEY (expense_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_chore_household   ON chore(household_id);
CREATE INDEX IF NOT EXISTS idx_expense_household ON expense(household_id);
CREATE INDEX IF NOT EXISTS idx_share_user        ON expense_share(user_id);
