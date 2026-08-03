# BR-665 — 도메인 설계

## 엔티티 / 값 객체

신규 엔티티·DB 테이블 없음. 기존 엔티티를 읽기만 한다.

| 도메인 | 엔티티 | 역할 |
|---|---|---|
| openChat | `OpenChatRoom` | 방 이름·설명·공개여부·lastMessageAt |
| openChat | `OpenChatParticipant` | 참여 여부, lastReadMessageId (unread 기준) |
| openChat | `OpenChatMessage` | id 기준 unread count 집계 |
| roommate | `RoommateChattingRoom` | host/guest User 참조, hostLeft/guestLeft |
| roommate | `RoommateChattingChat` | content·readByReceiver·createdDate |

---

## 애그리거트 경계

- `OpenChatRoom` 애그리거트: 기존과 동일. 변경 없음.
- `RoommateChattingRoom` 애그리거트: 기존과 동일. 변경 없음.
- 두 애그리거트를 **읽기 전용으로 조합**하는 로직은 `OpenChatRoomService`에 위치한다.
  - CLAUDE.md 원칙: 도메인 간 호출은 service 레벨에서만 허용.
  - `OpenChatRoomService`가 `RoommateChattingRoomRepository`, `RoommateChattingChatRepository`를 생성자 주입으로 참조한다.

---

## 연관관계

변경 없음. 기존 연관관계를 그대로 사용한다.

- `RoommateChattingRoom.host / guest`: `@ManyToOne LAZY` (기존)
- `RoommateChattingRoom.chattingChatList`: `@OneToMany LAZY` (기존, 직접 접근 금지 — bulk 쿼리 사용)

---

## DB 스키마 변경

없음.

---

## 도메인 계층 구조

### 신규 생성 파일

```
domain/openChat/
├── enums/
│   └── ChatCategory.java          ← NEW: OPEN_CHAT | ROOMMATE
└── dto/
    └── response/
        └── ResponseChatRoomListDto.java  ← NEW: 페이지 래퍼 + totalUnreadCount
```

```
domain/roommate/
└── repository/
    └── RoommateChattingChatQuerydslRepository.java       ← NEW: interface
    └── RoommateChattingChatQuerydslRepositoryImpl.java   ← NEW: bulk last-message·unread 쿼리
```

### 수정 파일

```
domain/openChat/
├── dto/response/ResponseOpenChatRoomDto.java             ← chatCategory 필드 추가
├── repository/
│   ├── OpenChatRoomQuerydslRepository.java               ← 메서드 시그니처에 keyword 추가
│   └── OpenChatRoomQuerydslRepositoryImpl.java           ← keyword BooleanExpression 추가
├── service/OpenChatRoomService.java                      ← MY 탭 통합, 반환 타입 변경
├── controller/OpenChatRoomController.java                ← keyword 파라미터, 반환 타입 변경
└── controller/OpenChatRoomApiSpecification.java          ← 인터페이스 시그니처 변경

domain/roommate/
└── repository/
    ├── RoommateChattingChatRepository.java               ← QueryDSL 인터페이스 상속 추가
    └── RoommateChattingRoomRepository.java               ← 활성 방 조회 JPQL 추가
```

---

## 클래스별 설계

### `ChatCategory` (신규 enum)

```java
public enum ChatCategory {
    OPEN_CHAT, ROOMMATE
}
```

### `ResponseChatRoomListDto` (신규 DTO)

```java
@Getter
@Builder
public class ResponseChatRoomListDto {
    private List<ResponseOpenChatRoomDto> content;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private int totalUnreadCount;

    public static ResponseChatRoomListDto of(List<ResponseOpenChatRoomDto> all, Pageable pageable, int totalUnreadCount) {
        // all: 병합·정렬 완료된 전체 목록. subList로 페이지 슬라이싱
    }
}
```

### `ResponseOpenChatRoomDto` 변경

- `chatCategory` 필드 추가 (`ChatCategory`)
- 기존 `from()` 팩토리에 `chatCategory = OPEN_CHAT` 고정
- 룸메 방용 신규 팩토리 메서드 `fromRoommate(...)` 추가:
  - `roomType = null`, `scope = null`
  - `isPublic = false`, `hasPassword = false`, `isJoined = true`
  - `currentParticipants = 2`, `maxParticipants = 2`
  - `chatCategory = ROOMMATE`

### `OpenChatRoomQuerydslRepository` / Impl 변경

기존 3개 메서드에 `String keyword` 파라미터 추가:

```java
List<OpenChatRoom> findMyRooms(Long userId, String keyword);
List<OpenChatRoom> findByDormitory(String dormType, String keyword);
List<OpenChatRoom> findAllPublicRooms(String keyword);
```

Impl에 공통 `BooleanExpression`:

```java
private BooleanExpression keywordContains(String keyword) {
    if (keyword == null || keyword.isBlank()) return null;
    return openChatRoom.name.containsIgnoreCase(keyword)
            .or(openChatRoom.description.containsIgnoreCase(keyword));
}
```

### `RoommateChattingChatQuerydslRepositoryImpl` (신규)

N+1 방지를 위한 bulk 쿼리 2개:

```java
// 방 ID 목록에서 마지막 메시지(최대 id) 1개씩 — Map<roomId, RoommateChattingChat>
Map<Long, RoommateChattingChat> findLastMessagesByRoomIds(List<Long> roomIds);

// 방별 안읽음 수 (member != userId AND readByReceiver = false) — Map<roomId, Long>
Map<Long, Long> countUnreadByRoomIdsAndUserId(List<Long> roomIds, Long userId);
```

`findLastMessagesByRoomIds` 구현: 서브쿼리로 방별 max(id) 추출 후 조인.

### `RoommateChattingRoomRepository` 변경

활성 방(본인이 나가지 않은 방) 조회용 JPQL 추가:

```java
@Query("""
    SELECT r FROM RoommateChattingRoom r
    JOIN FETCH r.host JOIN FETCH r.guest
    WHERE (r.host.id = :userId AND r.hostLeft = false)
       OR (r.guest.id = :userId AND r.guestLeft = false)
""")
List<RoommateChattingRoom> findActiveRoomsByUserId(@Param("userId") Long userId);
```

키워드 필터는 서비스 레이어 in-memory로 처리 (개인 룸메 방 수 ≪ 50으로 소규모 데이터).

### `OpenChatRoomService` 변경

- `getRooms(Long userId, OpenChatRoomTab tab, String keyword, Pageable pageable)` 시그니처 변경
- MY 탭 로직 교체: `toPageDtoWithUnread` 대신 `toUnifiedMyTab` private 메서드 신설
  - openChat 방 + roommate 방 fetch → `ResponseOpenChatRoomDto` 리스트로 변환 → 병합 정렬 → 페이지 슬라이싱
  - `totalUnreadCount` = openChat unread 합 + roommate unread 합
- ALL / DORMITORY 탭: keyword를 레포지토리로 위임, `totalUnreadCount = 0`
- 반환 타입: `Page<ResponseOpenChatRoomDto>` → `ResponseChatRoomListDto`

### `OpenChatRoomController` / `ApiSpecification` 변경

```java
@GetMapping
public ResponseEntity<ResponseChatRoomListDto> getRooms(
        @AuthenticationPrincipal CustomUserDetails user,
        @RequestParam OpenChatRoomTab tab,
        @RequestParam(required = false) String keyword,
        Pageable pageable)
```

---

## MY 탭 병합 페이지네이션 트레이드오프

두 도메인의 데이터를 DB 레벨에서 합산 페이지네이션하려면 UNION 쿼리 또는 별도 뷰가 필요하다. 이는 과도한 복잡도이므로 **in-memory 병합 후 subList 페이지네이션** 방식을 채택한다.

- **전제**: MY 탭은 개인 참여 방 목록이므로 총 건수 ≪ 100 (실용 범위)
- **비용**: DB 쿼리 결과 전체를 메모리에 올린 뒤 정렬·슬��이싱
- **이득**: 단순한 코드, 두 도메인 독립 유지

---

## 비목표

- roommate 도메인 기존 API 변경 없음
- openChat 방 상세 조회·메시지 API 변경 없음
- `RoommateChattingRoom` 엔티티에 `lastMessageAt` 컬럼 추가 안 함 (bulk 쿼리로 조회)
- 캐싱, 정렬 파라미터 추가 없음
- 키워드 full-text 인덱스, 한글 초성 검색 등 고급 검색 없음
