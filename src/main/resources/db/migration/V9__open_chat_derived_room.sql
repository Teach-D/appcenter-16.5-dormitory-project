ALTER TABLE open_chat_room
    ADD COLUMN room_type VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN parent_room_id BIGINT NULL;

CREATE TABLE IF NOT EXISTS open_chat_invitation (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id          BIGINT      NOT NULL,
    inviter_user_id  BIGINT      NOT NULL,
    invitee_user_id  BIGINT      NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at       DATETIME(6) NOT NULL
);
