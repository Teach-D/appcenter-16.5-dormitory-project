새로운 Flyway 마이그레이션 SQL 파일을 생성해줘. 내용: $ARGUMENTS

## 작업 순서

1. `src/main/resources/db/migration/` 디렉토리에서 기존 마이그레이션 파일 목록을 확인해서 다음 버전 번호를 결정해.

2. 파일명 형식: `V{다음버전}__{snake_case_설명}.sql`
   - 예: `V3__add_tip_table.sql`

3. SQL 작성 시 MySQL 문법 사용 (primary DB):
   - `CREATE TABLE IF NOT EXISTS`
   - `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`
   - 외래키 제약조건 포함
   - 인덱스 추가 고려 (자주 조회되는 컬럼)

4. 테이블 컬럼 컨벤션:
   ```sql
   CREATE TABLE IF NOT EXISTS table_name (
       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
       -- 비즈니스 컬럼들
       created_date DATETIME(6),
       modified_date DATETIME(6)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   ```

5. 관련 JPA 엔티티의 `@Table(name = "...")` 과 컬럼명이 일치하는지 확인해.

6. 생성된 파일 경로와 SQL 내용을 출력해줘.

## 주의사항
- 마이그레이션 파일은 한번 배포되면 수정 불가 (새 버전으로 변경사항 추가)
- 로컬에서는 flyway.enabled=false 이므로 실제 적용은 프로덕션 배포 시
- 컬럼 삭제는 데이터 손실 위험 - 먼저 팀에 확인 필요
