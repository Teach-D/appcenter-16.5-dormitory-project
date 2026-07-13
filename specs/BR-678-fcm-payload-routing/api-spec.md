# BR-678 FCM 페이로드 라우팅 — API 명세

> 이 BR은 신규 HTTP 엔드포인트를 추가하지 않는다.
> 변경은 FCM 파이프라인 내부(Outbox 적재 → 발송 빌더)에 국한된다.

## 변경 없는 기존 엔드포인트

다음 엔드포인트는 시그니처·응답 스키마 변경 없음. 참고용으로만 기재.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/fcm/token` | FCM 토큰 등록 |
| `GET` | `/fcm/stats` | 당일 발송 통계 조회 |
| `POST` | `/fcm/send/all` | 전체 유저 푸시 발송 (routing 대상 아님) |
| `GET` | `/fcm/dlq` | DLQ 목록 조회 |
| `POST` | `/fcm/dlq/{outboxId}/retry` | DLQ 항목 재시도 |

## 내부 변경 요약

FCM 메시지 빌드 로직이 다음과 같이 변경된다 (HTTP 계층 영향 없음).

| 알림 종류 | 추가 필드 | 값 예시 |
|-----------|-----------|---------|
| 공지사항 | `apns.aps.thread-id`, `android.notification.tag`, `data` | `"notice_5678"`, `{type:"NOTICE", noticeId:"5678"}` |
| 채팅 | `apns.aps.thread-id`, `android.notification.tag`, `data` | `"chat_room_1234"`, `{type:"CHAT", chatRoomId:"1234"}` |
| 기타(전체 발송 등) | 변경 없음 | — |
