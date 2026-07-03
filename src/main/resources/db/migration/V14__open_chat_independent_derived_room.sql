DROP TABLE IF EXISTS open_chat_invitation;

ALTER TABLE open_chat_room DROP FOREIGN KEY fk_room_parent;
ALTER TABLE open_chat_room DROP COLUMN parent_room_id;

ALTER TABLE open_chat_room ADD COLUMN password VARCHAR(50) NULL;
ALTER TABLE open_chat_room ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT TRUE;
