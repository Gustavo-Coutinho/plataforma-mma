-- Add total_logins column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_logins INTEGER DEFAULT 0;

-- Initialize total_logins for existing users
UPDATE users SET total_logins = 0 WHERE total_logins IS NULL;
