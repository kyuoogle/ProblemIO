/* =========================================================
   Problem.io - Schema DDL (clean version)
   - Engine: InnoDB
   - Charset: utf8mb4
   - Note: FK 의존 순서 고려하여 생성
   ========================================================= */

CREATE DATABASE IF NOT EXISTS `problemio`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `problemio`;

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1) users : 회원
-- =========================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `email` VARCHAR(100) NOT NULL COMMENT '로그인 이메일(유니크)',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
  `nickname` VARCHAR(50) NOT NULL COMMENT '닉네임(유니크)',
  `profile_image_url` VARCHAR(500) DEFAULT NULL COMMENT '프로필 이미지 URL',
  `status_message` VARCHAR(255) DEFAULT NULL COMMENT '상태 메시지',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '탈퇴 여부(소프트 삭제)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  `profile_theme` VARCHAR(255) DEFAULT NULL COMMENT '프로필 테마(확장용)',
  `avatar_decoration` VARCHAR(255) DEFAULT NULL COMMENT '아바타 데코(확장용)',
  `popover_decoration` VARCHAR(255) DEFAULT NULL COMMENT '팝오버 데코(확장용)',
  `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '유저 권한 (ROLE_USER, ROLE_ADMIN)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  UNIQUE KEY `uq_users_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 2) quizzes : 퀴즈(문제 세트)
-- =========================================================
DROP TABLE IF EXISTS `quizzes`;
CREATE TABLE `quizzes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `user_id` BIGINT NOT NULL COMMENT '작성자(회원)',
  `title` VARCHAR(100) NOT NULL COMMENT '퀴즈 제목',
  `description` TEXT NOT NULL COMMENT '퀴즈 설명',
  `thumbnail_url` VARCHAR(500) NOT NULL COMMENT '썸네일 이미지 URL',
  `is_public` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '공개 여부',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '좋아요 수(반정규화)',
  `play_count` INT NOT NULL DEFAULT 0 COMMENT '플레이 수(반정규화)',
  `is_hidden` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '숨김 처리 여부 (관리자/제작자만 볼 수 있음)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `idx_quizzes_user` (`user_id`),
  KEY `idx_quizzes_popular` (`like_count`, `play_count`),
  CONSTRAINT `fk_quizzes_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 3) questions : 퀴즈 내 문항
-- =========================================================
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `quiz_id` BIGINT NOT NULL COMMENT '소속 퀴즈',
  `question_order` INT NOT NULL COMMENT '문항 순서(1,2,3...)',
  `image_url` VARCHAR(500) NOT NULL COMMENT '문항 이미지 URL',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '힌트/설명',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_questions_quiz_order` (`quiz_id`, `question_order`) COMMENT '퀴즈 내 문항 순서 유니크',
  KEY `idx_questions_quiz` (`quiz_id`),
  CONSTRAINT `fk_questions_quiz`
    FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 4) question_answers : 문항 정답 후보(복수 정답 지원)
-- =========================================================
DROP TABLE IF EXISTS `question_answers`;
CREATE TABLE `question_answers` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `question_id` BIGINT NOT NULL COMMENT '소속 문항',
  `answer_text` VARCHAR(100) NOT NULL COMMENT '정답 텍스트',
  `sort_order` INT NOT NULL DEFAULT 1 COMMENT '대표 정답 우선순위(낮을수록 우선)',
  PRIMARY KEY (`id`),
  KEY `idx_answers_question` (`question_id`),
  CONSTRAINT `fk_answers_question`
    FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 5) submissions : 퀴즈 풀이 결과(제출)
--   - 비회원 풀이 허용: user_id NULL 가능
-- =========================================================
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `quiz_id` BIGINT NOT NULL COMMENT '대상 퀴즈',
  `user_id` BIGINT DEFAULT NULL COMMENT '풀이자(비회원은 NULL)',
  `total_questions` INT NOT NULL COMMENT '전체 문항 수',
  `correct_count` INT NOT NULL COMMENT '정답 문항 수',
  `submitted_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '제출 시각',
  PRIMARY KEY (`id`),
  KEY `idx_submissions_quiz` (`quiz_id`),
  KEY `idx_submissions_user` (`user_id`),
  KEY `idx_submissions_submitted_user` (`submitted_at`, `user_id`) COMMENT '기간+유저 기반 집계(랭킹용)',
  CONSTRAINT `fk_submissions_quiz`
    FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_submissions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 6) submission_details : 제출 상세(문항별 답안/정오)
-- =========================================================
DROP TABLE IF EXISTS `submission_details`;
CREATE TABLE `submission_details` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `submission_id` BIGINT NOT NULL COMMENT '소속 제출',
  `question_id` BIGINT NOT NULL COMMENT '대상 문항',
  `user_answer` VARCHAR(255) NOT NULL COMMENT '사용자 답안',
  `is_correct` TINYINT(1) NOT NULL COMMENT '정답 여부',
  PRIMARY KEY (`id`),
  KEY `idx_subdetail_sub` (`submission_id`),
  KEY `idx_subdetail_question` (`question_id`),
  CONSTRAINT `fk_subdetail_submission`
    FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_subdetail_question`
    FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 7) comments : 댓글(회원/게스트 통합)
--   - user_id NULL 가능(게스트)
--   - 대댓글: parent_comment_id / root_comment_id (self FK)
--   - is_deleted: 소프트 삭제 플래그
-- =========================================================
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `quiz_id` BIGINT NOT NULL COMMENT '대상 퀴즈',
  `parent_comment_id` BIGINT DEFAULT NULL COMMENT '부모 댓글(대댓글)',
  `root_comment_id` BIGINT DEFAULT NULL COMMENT '루트 댓글(스레드 최상단)',
  `user_id` BIGINT DEFAULT NULL COMMENT '작성자(회원이면 user_id, 게스트면 NULL)',
  `guest_nickname` VARCHAR(50) DEFAULT NULL COMMENT '게스트 닉네임',
  `guest_password_hash` VARCHAR(100) DEFAULT NULL COMMENT '게스트 비밀번호 해시(삭제/수정 인증용)',
  `content` TEXT NOT NULL COMMENT '댓글 내용',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '좋아요 수(반정규화)',
  `writer_ip` VARCHAR(45) DEFAULT NULL COMMENT '작성자 IP(옵션)',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부(소프트 삭제)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `idx_comments_quiz` (`quiz_id`, `created_at`) COMMENT '퀴즈별 최신 댓글 조회',
  KEY `idx_comments_user` (`user_id`) COMMENT '유저별 댓글 조회',
  KEY `idx_comments_root_created` (`root_comment_id`, `created_at`) COMMENT '스레드(루트) 기반 정렬',
  KEY `idx_comments_parent_created` (`parent_comment_id`, `created_at`) COMMENT '부모 댓글 하위 정렬',
  CONSTRAINT `fk_comments_quiz`
    FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_parent_comment`
    FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_root_comment`
    FOREIGN KEY (`root_comment_id`) REFERENCES `comments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 8) comment_likes : 댓글 좋아요(회원만)
--   - (user_id, comment_id) 복합 PK로 중복 좋아요 방지
-- =========================================================
DROP TABLE IF EXISTS `comment_likes`;
CREATE TABLE `comment_likes` (
  `user_id` BIGINT NOT NULL COMMENT '좋아요 누른 회원',
  `comment_id` BIGINT NOT NULL COMMENT '대상 댓글',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`user_id`, `comment_id`),
  KEY `idx_comment_likes_comment` (`comment_id`),
  CONSTRAINT `fk_comment_likes_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_likes_comment`
    FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 9) follows : 팔로우 관계(회원만)
--   - (follower_id, following_id) 복합 PK로 중복 팔로우 방지
-- =========================================================
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
  `follower_id` BIGINT NOT NULL COMMENT '팔로우 하는 회원',
  `following_id` BIGINT NOT NULL COMMENT '팔로우 당하는 회원',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`follower_id`, `following_id`),
  KEY `idx_follows_following` (`following_id`),
  CONSTRAINT `fk_follows_follower`
    FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_follows_following`
    FOREIGN KEY (`following_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 10) quiz_likes : 퀴즈 좋아요(회원만)
--   - (user_id, quiz_id) 복합 PK로 중복 좋아요 방지
-- =========================================================
DROP TABLE IF EXISTS `quiz_likes`;
CREATE TABLE `quiz_likes` (
  `user_id` BIGINT NOT NULL COMMENT '좋아요 누른 회원',
  `quiz_id` BIGINT NOT NULL COMMENT '대상 퀴즈',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`user_id`, `quiz_id`),
  KEY `idx_quiz_likes_quiz` (`quiz_id`),
  CONSTRAINT `fk_quiz_likes_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_quiz_likes_quiz`
    FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 11) refresh_tokens : 리프레시 토큰 저장(서버측)
-- =========================================================
DROP TABLE IF EXISTS `refresh_tokens`;
CREATE TABLE `refresh_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `user_id` BIGINT NOT NULL COMMENT '대상 회원',
  `token_value` VARCHAR(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '리프레시 토큰 값',
  `expires_at` DATETIME NOT NULL COMMENT '만료 시각',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`id`),
  KEY `fk_refresh_tokens_user` (`user_id`),
  CONSTRAINT `fk_refresh_tokens_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 12) challenges : 이벤트 챌린지 (기존 퀴즈 재활용)
-- =========================================================
DROP TABLE IF EXISTS `challenges`;
CREATE TABLE `challenges` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `title` VARCHAR(255) NOT NULL COMMENT '챌린지 제목 ',
  `description` TEXT COMMENT '챌린지 설명',
  `challenge_type` VARCHAR(20) NOT NULL COMMENT '유형: TIME_ATTACK, SURVIVAL',
  `target_quiz_id` BIGINT NOT NULL COMMENT '문제 은행으로 쓸 원본 퀴즈 ID',
  `time_limit` INT DEFAULT 0 COMMENT '제한 시간 (초단위, 타임어택용)',
  `start_at` DATETIME DEFAULT NULL COMMENT '이벤트 시작 일시',
  `end_at` DATETIME DEFAULT NULL COMMENT '이벤트 종료 일시',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `idx_challenges_period` (`start_at`, `end_at`),
  CONSTRAINT `fk_challenges_quiz`
    FOREIGN KEY (`target_quiz_id`) REFERENCES `quizzes` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 기존 submissions 테이블에 챌린지용 컬럼 추가
ALTER TABLE `submissions`
ADD COLUMN `challenge_id` BIGINT DEFAULT NULL COMMENT '챌린지 ID (NULL이면 일반 퀴즈 풀이)',
ADD COLUMN `play_time` DECIMAL(10, 3) DEFAULT 0 COMMENT '풀이 소요 시간(초) - 랭킹 동점자 처리용';

-- 외래키 제약조건 추가 (선택사항, 데이터 무결성용)
ALTER TABLE `submissions`
ADD CONSTRAINT `fk_submissions_challenge`
FOREIGN KEY (`challenge_id`) REFERENCES `challenges` (`id`)
ON DELETE SET NULL ON UPDATE CASCADE;

-- 랭킹 조회를 빠르게 하기 위한 인덱스 추가 (성능 최적화)
CREATE INDEX `idx_submissions_challenge_ranking` ON `submissions` (`challenge_id`, `correct_count` DESC, `play_time` ASC);

-- =========================================================
-- 13) challenge_rankings : 챌린지 랭킹 박제 (이벤트 종료 후 생성)
-- =========================================================
DROP TABLE IF EXISTS `challenge_rankings`;
CREATE TABLE `challenge_rankings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `challenge_id` BIGINT NOT NULL COMMENT '대상 챌린지',
  `user_id` BIGINT NOT NULL COMMENT '대상 유저',
  `ranking` INT NOT NULL COMMENT '최종 순위',
  `score` DOUBLE DEFAULT NULL COMMENT '점수(정답 수 등)',
  `play_time` DECIMAL(10, 3) DEFAULT NULL COMMENT '소요 시간(초)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일(박제 시점)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_challenge_user` (`challenge_id`, `user_id`),
  KEY `idx_challenge_ranking` (`challenge_id`, `ranking`),
  CONSTRAINT `fk_challenge_rankings_challenge`
    FOREIGN KEY (`challenge_id`) REFERENCES `challenges` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_challenge_rankings_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;