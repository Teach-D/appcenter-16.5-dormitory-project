ALTER TABLE open_chat_participant ADD COLUMN is_host BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE open_chat_participant p
JOIN open_chat_room r ON p.room_id = r.id
SET p.is_host = TRUE
WHERE p.user_id = r.host_user_id;

ALTER TABLE open_chat_room DROP COLUMN host_user_id;
