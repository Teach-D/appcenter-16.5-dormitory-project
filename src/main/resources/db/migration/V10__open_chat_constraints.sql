ALTER TABLE open_chat_invitation
    ADD CONSTRAINT fk_invitation_room
        FOREIGN KEY (room_id) REFERENCES open_chat_room (id) ON DELETE CASCADE;

ALTER TABLE open_chat_invitation
    ADD CONSTRAINT uq_invitation_room_invitee_status
        UNIQUE (room_id, invitee_user_id, status);

ALTER TABLE open_chat_room
    ADD CONSTRAINT fk_room_parent
        FOREIGN KEY (parent_room_id) REFERENCES open_chat_room (id) ON DELETE SET NULL;
