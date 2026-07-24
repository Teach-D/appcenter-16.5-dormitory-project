ALTER TABLE roommate_chatting_chat MODIFY COLUMN member_id BIGINT NULL;
ALTER TABLE roommate_chatting_chat ADD COLUMN is_system TINYINT(1) NOT NULL DEFAULT 0;
