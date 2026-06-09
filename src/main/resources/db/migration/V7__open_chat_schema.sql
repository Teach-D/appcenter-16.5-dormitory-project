CREATE TABLE IF NOT EXISTS open_chat_room (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(30)  NOT NULL,
    description      VARCHAR(100),
    scope            VARCHAR(20)  NOT NULL,
    max_participants INT          NOT NULL,
    creator_dormitory VARCHAR(50),
    host_user_id     BIGINT,
    last_message_at  DATETIME(6),
    is_official      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by       BIGINT,
    created_date     DATETIME(6),
    modified_date    DATETIME(6)
);

CREATE TABLE IF NOT EXISTS open_chat_participant (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id              BIGINT       NOT NULL,
    user_id              BIGINT       NOT NULL,
    notification_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    joined_at            DATETIME(6)  NOT NULL,
    UNIQUE KEY uq_room_user (room_id, user_id)
);

CREATE TABLE IF NOT EXISTS open_chat_message (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id    BIGINT       NOT NULL,
    sender_id  BIGINT       NOT NULL,
    content    TEXT         NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL
);

INSERT INTO open_chat_room (name, description, scope, max_participants, creator_dormitory, host_user_id, is_official, created_by, created_date, modified_date) VALUES
('전체 오픈채팅', '기숙사 전체 학생 소통 공간', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW()),
('1기숙사 채팅방', '1기숙사 학생 전용 채팅방', 'DORMITORY', 100, 'DORM_1', NULL, TRUE, NULL, NOW(), NOW()),
('2기숙사 채팅방', '2기숙사 학생 전용 채팅방', 'DORMITORY', 100, 'DORM_2', NULL, TRUE, NULL, NOW(), NOW()),
('3기숙사 채팅방', '3기숙사 학생 전용 채팅방', 'DORMITORY', 100, 'DORM_3', NULL, TRUE, NULL, NOW(), NOW()),
('정보공유', '기숙사 생활 정보를 공유하는 공간', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW()),
('중고거래', '기숙사 내 중고거래 채팅방', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW()),
('스터디그룹', '함께 공부하는 스터디 모집 채팅방', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW()),
('맛집추천', '기숙사 주변 맛집을 공유하는 채팅방', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW()),
('취미생활', '취미를 공유하고 함께 즐기는 채팅방', 'ALL', 100, NULL, NULL, TRUE, NULL, NOW(), NOW());
