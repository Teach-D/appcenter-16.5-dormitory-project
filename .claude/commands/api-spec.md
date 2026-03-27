$ARGUMENTS 도메인(또는 현재 작업 중인 코드)의 API 명세를 정리해줘.

## 출력 형식

각 엔드포인트에 대해 다음 정보를 마크다운 테이블로 정리:

### {DomainName} API

| 메서드 | URL | 권한 | 설명 | Request | Response |
|--------|-----|------|------|---------|----------|
| GET | /domain/{id} | USER | 단건 조회 | - | ResponseDto |
| POST | /domain | USER | 생성 | RequestCreateDto | ResponseDto |
| ...   | ... | ...  | ... | ... | ... |

### Request DTO 상세
각 DTO 필드와 validation 조건 목록

### Response DTO 상세
각 DTO 필드 목록

### 에러 응답
발생 가능한 예외 상황과 HTTP 상태코드

## 확인할 파일
- `controller/{Domain}Controller.java` - 엔드포인트 정의
- `controller/{Domain}ApiSpecification.java` - Swagger 설명
- `dto/request/*.java` - 요청 DTO
- `dto/response/*.java` - 응답 DTO
- `global/config/SecurityConfig.java` - 인증/인가 설정

이 명세는 프론트엔드 팀과의 협업이나 PR 설명에 활용할 수 있게 간결하고 명확하게 작성해줘.
