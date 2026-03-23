-- ============================================================
-- V2__refresh_token_table.sql
-- 멀티 디바이스 로그인 지원을 위한 리프레시 토큰 테이블 분리
-- ============================================================

ALTER TABLE user DROP COLUMN refresh_token;

CREATE TABLE refresh_token (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    token      VARCHAR(500) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE = InnoDB;
