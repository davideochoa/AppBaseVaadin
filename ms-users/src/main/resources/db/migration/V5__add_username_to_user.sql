ALTER TABLE app_user ADD COLUMN username VARCHAR(50);

UPDATE app_user SET username = LOWER(SPLIT_PART(email, '@', 1)) WHERE username IS NULL;

ALTER TABLE app_user ALTER COLUMN username SET NOT NULL;
ALTER TABLE app_user ADD CONSTRAINT uk_app_user_username UNIQUE (username);
