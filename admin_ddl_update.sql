-- Add role column to users table
ALTER TABLE `users`
ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '유저 권한 (ROLE_USER, ROLE_ADMIN)';

-- Add is_hidden column to quizzes table
ALTER TABLE `quizzes`
ADD COLUMN `is_hidden` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '숨김 처리 여부 (관리자/제작자만 볼 수 있음)';
