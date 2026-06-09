CREATE TABLE student_id_disclosure_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date DATETIME(6),
    modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_disclosure_request (requester_id, target_id, room_id)
);
