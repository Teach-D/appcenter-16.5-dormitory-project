---
name: openchat-image-message
description: openChat 이미지 전송 기능 (#639) 구현 완료, ImageType.OPEN_CHAT_MESSAGE 추가, ErrorCode IMAGE_INVALID_FORMAT(6005) 추가
metadata:
  type: project
---

# openChat 이미지 전송 기능 (#639)

## 구현 내용
- `ImageType.OPEN_CHAT_MESSAGE` 추가 (prefix: `open_chat_message`)
- `ImageService`에 `OPEN_CHAT_MESSAGE` 분기 추가 (createDirectoryPath, createImageName, findStaticImageUrl)
- `ErrorCode.IMAGE_INVALID_FORMAT(BAD_REQUEST, 6005)` 추가
- `ResponseOpenChatMessageDto`에 `imageUrls: List<String>` 필드 추가 및 `from()` 4파라미터 오버로드
- `OpenChatMessageService.sendImageMessage()` 추가
  - 허용 확장자: .jpg, .jpeg, .png, .gif, .webp
  - 0개 → VALIDATION_ERROR, 포맷 위반 → IMAGE_INVALID_FORMAT
  - content="" (NOT NULL 제약 준수)
  - lastMessage = "[이미지]"
  - N+1 방지: getMessages()에서 IMAGE 타입 배치 조회 (findByImageTypeAndEntityIdIn)
- `OpenChatMessageController.sendImageMessage()` 추가: POST /{roomId}/messages/image → 201
- `OpenChatMessageApiSpecification`에 Swagger 어노테이션 추가

## 테스트 파일
- fixture: OpenChatImageMessageFixture (createRoom, createParticipant, createImageMessage, create*File 등)
- service: OpenChatImageMessageServiceTest (12개 케이스)
- controller: OpenChatImageMessageControllerTest (10개 케이스, @WebMvcTest 패턴)

## WebMvcTest 패턴 (컨트롤러 테스트)
- `@MockBean SlackErrorNotifier` 필수 (GlobalExceptionHandler 의존)
- `@MockBean JpaMetamodelMappingContext` 필수
- `@MockBean ImageService`, `@MockBean ImageRepository` 필수
- `@BeforeEach`에서 `SecurityContextHolder`에 `CustomUserDetails` 세팅 필수

**왜:** @WebMvcTest는 @ControllerAdvice도 로드하므로 GlobalExceptionHandler의 SlackErrorNotifier가 필요함.
**적용 방법:** openChat 컨트롤러 테스트 작성 시 위 패턴 적용.
