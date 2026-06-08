ALTER TABLE open_chat_room
    ADD COLUMN last_message VARCHAR(500) NULL;

ALTER TABLE open_chat_participant
    ADD COLUMN last_read_message_id BIGINT NULL;
