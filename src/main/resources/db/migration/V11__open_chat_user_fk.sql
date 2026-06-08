ALTER TABLE open_chat_invitation
    ADD CONSTRAINT fk_invitation_inviter
        FOREIGN KEY (inviter_user_id) REFERENCES user(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_invitation_invitee
        FOREIGN KEY (invitee_user_id) REFERENCES user(id) ON DELETE CASCADE;
