UPDATE crawled_announcement
SET schedule_extract_retry_count = 0
WHERE schedule_extract_retry_count IS NULL;

ALTER TABLE crawled_announcement
    MODIFY schedule_extract_retry_count INT NOT NULL DEFAULT 0;