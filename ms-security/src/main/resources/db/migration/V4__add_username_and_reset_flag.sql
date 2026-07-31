ALTER TABLE user_security ADD COLUMN username VARCHAR(50);
ALTER TABLE user_security ADD COLUMN must_reset_password BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE user_security SET username = LOWER(SPLIT_PART(email, '@', 1)) WHERE username IS NULL;

ALTER TABLE user_security ALTER COLUMN username SET NOT NULL;
ALTER TABLE user_security ADD CONSTRAINT uk_user_security_username UNIQUE (username);
