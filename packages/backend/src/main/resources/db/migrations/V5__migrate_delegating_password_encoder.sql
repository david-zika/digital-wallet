-- Migrate existing BCrypt password hashes to DelegatingPasswordEncoder format.
-- Spring Security's DelegatingPasswordEncoder expects an algorithm identifier prefix
-- such as {bcrypt} before the hash so it can route to the correct PasswordEncoder.
-- Rows that already carry a prefix (e.g. from a previous migration run) are left untouched.
UPDATE users
SET password = CONCAT('{bcrypt}', password)
WHERE password NOT LIKE '{%}%';

