# 사용자 차단 해제 API 명세서 (BR-697)

> Base URL: `http://localhost:8080`  
> 인증: 모든 엔드포인트에 Bearer Token(JWT) 필요

---

## 차단 해제

| 항목 | 내용 |
|------|------|
| **메서드** | `DELETE` |
| **경로** | `/block/{targetUserId}` |
| **인증** | Bearer Token (JWT) |
| **설명** | 인증된 사용자가 자신이 차단한 상대방을 차단 해제한다. 해제 후 상대방은 PERSONAL 채팅방 생성 및 메시지 전송이 다시 가능해진다. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `targetUserId` | `Long` | ✅ | 차단 해제할 대상 사용자 ID |

#### Request Body

없음

### Response

#### 성공 응답 — `204 No Content`

응답 바디 없음

#### 에러 응답

에러 응답 형식:
```json
{
  "code": 26001,
  "name": "USER_BLOCK_CANNOT_BLOCK_SELF",
  "message": "[Block] 자기 자신을 차단할 수 없습니다."
}
```

| 상태 코드 | 코드명 | code | 발생 조건 |
|-----------|--------|------|-----------|
| `400 Bad Request` | `USER_BLOCK_CANNOT_BLOCK_SELF` | 26001 | `targetUserId`가 본인 ID와 동일한 경우 |
| `401 Unauthorized` | — | — | JWT 토큰 없거나 유효하지 않은 경우 |
| `404 Not Found` | `USER_BLOCK_NOT_FOUND` | 26004 | 차단 기록이 존재하지 않는 경우 (미차단 상대 또는 이미 해제) |
