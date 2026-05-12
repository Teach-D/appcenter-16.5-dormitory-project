-- crawled_announcement 테이블 (JOINED 전략 자식 테이블)
ALTER TABLE crawled_announcement
    ADD COLUMN schedule_extract_status      VARCHAR(32)  NULL     DEFAULT 'PENDING' COMMENT 'AI 일정 추출 상태 (PENDING/SUCCESS/NO_SCHEDULE/FAILED)',
    ADD COLUMN schedule_extract_retry_count INT          NOT NULL DEFAULT 0         COMMENT 'AI 일정 추출 재시도 횟수',
    ADD COLUMN schedule_extract_last_error  VARCHAR(500) NULL                       COMMENT '마지막 추출 실패 오류 메시지',
    ADD COLUMN schedule_extracted_at        DATETIME(6)  NULL                       COMMENT '마지막 추출 시도 시각';

UPDATE crawled_announcement SET schedule_extract_status = 'PENDING' WHERE schedule_extract_status IS NULL;

-- 마이그레이션 직후 백로그 폭주 방지: 3일 이전 공지는 추출 대상에서 제외
UPDATE crawled_announcement
SET schedule_extract_status = 'NO_SCHEDULE',
    schedule_extracted_at   = NOW(6)
WHERE crawled_date < (CURRENT_DATE - INTERVAL 3 DAY);

CREATE INDEX idx_crawled_extract_status ON crawled_announcement (schedule_extract_status);

-- calender 테이블
ALTER TABLE calender
    ADD COLUMN source_announcement_id BIGINT     NULL     COMMENT '원본 크롤링 공지사항 ID',
    ADD COLUMN ai_generated           TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'AI 자동 생성 여부';

CREATE INDEX idx_calender_source_ai ON calender (source_announcement_id, ai_generated);