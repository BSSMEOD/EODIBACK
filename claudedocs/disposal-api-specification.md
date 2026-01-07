# 폐기 관련 API 명세서

## 목차
1. [폐기 보류 사유 제출 API](#1-폐기-보류-사유-제출-api)
2. [폐기 보류 사유 조회 API](#2-폐기-보류-사유-조회-api)
3. [폐기 기간 연장 API](#3-폐기-기간-연장-api)
4. [자동 폐기 스케줄러](#4-자동-폐기-스케줄러)

---

## 1. 폐기 보류 사유 제출 API

### 기본 정보
- **Endpoint**: `POST /items/{item-id}/disposal-reason`
- **설명**: 폐기 예정 물품에 대한 보류 사유를 제출하고 폐기 기간을 연장합니다.
- **권한**: 선생님(TEACHER) 또는 관리자(ADMIN)
- **인증**: 필수 (JWT Bearer Token)

### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| item-id | Long | ✓ | 폐기 보류할 물품 ID | 1 |

### Request Body
```json
{
  "reason": "학생이 찾을 가능성이 있어 보류합니다.",
  "days": 5
}
```

#### Request 필드 상세

| 필드 | 타입 | 필수 | 제약조건 | 설명 | 예시 |
|------|------|------|---------|------|------|
| reason | String | ✓ | - 공백 불가<br>- 최대 1000자 | 폐기 보류 사유 | "학생이 찾을 가능성이 있어 보류합니다." |
| days | Integer | ✓ | - 양수<br>- 최대 365일 | 폐기 기간 연장 일수 | 5 |

#### Validation Rules
- `reason`: NotBlank, 최대 1000자
- `days`: NotNull, Positive (1 이상), Max 365

### Response

#### 201 Created - 성공
```json
{
  "message": "보류 사유가 성공적으로 제출되었습니다."
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| message | String | 성공 메시지 |

#### 400 Bad Request - 유효성 검증 실패
```json
{
  "message": "폐기 예정 상태의 물품만 보류할 수 있습니다."
}
```

**발생 경우**:
- 물품 상태가 `TO_BE_DISCARDED`가 아닌 경우
- 필수 값 누락 또는 유효성 검증 실패
- 연장 일수가 1 미만 또는 365 초과

#### 403 Forbidden - 권한 없음
```json
{
  "message": "선생님 또는 관리자 권한이 필요합니다."
}
```

**발생 경우**:
- 일반 사용자(USER)가 요청한 경우

#### 404 Not Found - 물품 없음
```json
{
  "message": "물품을 찾을 수 없습니다."
}
```

**발생 경우**:
- 존재하지 않는 item-id로 요청한 경우

### 비즈니스 로직
1. 물품 조회 (ItemFacade)
2. 권한 검증: 선생님 또는 관리자 확인
3. 물품 상태 검증: TO_BE_DISCARDED 확인
4. 폐기 보류 사유 생성 및 저장
5. 폐기 예정일 자동 계산 및 설정 (현재 시각 + days)

### 예시

#### 성공 케이스
**Request**
```bash
curl -X POST "http://localhost:8080/items/1/disposal-reason" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5..." \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "학생이 찾을 가능성이 있어 보류합니다.",
    "days": 5
  }'
```

**Response (201)**
```json
{
  "message": "보류 사유가 성공적으로 제출되었습니다."
}
```

#### 실패 케이스 - 권한 없음
**Request**
```bash
curl -X POST "http://localhost:8080/items/1/disposal-reason" \
  -H "Authorization: Bearer [USER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "보류 사유",
    "days": 5
  }'
```

**Response (403)**
```json
{
  "message": "선생님 또는 관리자 권한이 필요합니다."
}
```

---

## 2. 폐기 보류 사유 조회 API

### 기본 정보
- **Endpoint**: `GET /items/{item-id}/disposal-reason`
- **설명**: 특정 물품의 최신 폐기 보류 사유를 조회합니다.
- **권한**: 인증 필요 (역할 제한 없음)
- **인증**: 필수 (JWT Bearer Token)

### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| item-id | Long | ✓ | 조회할 물품 ID | 1 |

### Request Body
없음 (GET 요청)

### Response

#### 200 OK - 성공
```json
{
  "item_id": 1,
  "image": "https://storage.example.com/items/abc123.jpg",
  "teacher_name": "육은찬",
  "reason": "학생이 찾을 가능성이 있어 보류합니다.",
  "extension_days": 5
}
```

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| item_id | Long | 물품 ID | 1 |
| image | String | 물품 이미지 URL | "https://storage.example.com/items/abc123.jpg" |
| teacher_name | String | 보류 사유를 제출한 선생님 이름 | "육은찬" |
| reason | String | 폐기 보류 사유 | "학생이 찾을 가능성이 있어 보류합니다." |
| extension_days | Integer | 폐기 기간 연장 일수 | 5 |

#### 404 Not Found - 조회 실패
```json
{
  "message": "해당 물품의 폐기 보류 사유를 찾을 수 없습니다."
}
```

**발생 경우**:
- 존재하지 않는 item-id로 요청
- 해당 물품에 폐기 보류 사유가 없는 경우

### 비즈니스 로직
1. 물품 조회 (ItemFacade)
2. 최신 폐기 보류 사유 조회 (createdAt 기준 내림차순)
3. 응답 DTO 변환

### 예시

#### 성공 케이스
**Request**
```bash
curl -X GET "http://localhost:8080/items/1/disposal-reason" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5..."
```

**Response (200)**
```json
{
  "item_id": 1,
  "image": "https://storage.example.com/items/abc123.jpg",
  "teacher_name": "육은찬",
  "reason": "학생이 찾을 가능성이 있어 보류합니다.",
  "extension_days": 5
}
```

#### 실패 케이스 - 보류 사유 없음
**Request**
```bash
curl -X GET "http://localhost:8080/items/999/disposal-reason" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5..."
```

**Response (404)**
```json
{
  "message": "해당 물품의 폐기 보류 사유를 찾을 수 없습니다."
}
```

---

## 3. 폐기 기간 연장 API

### 기본 정보
- **Endpoint**: `PATCH /items/{item-id}/discarded`
- **설명**: 폐기 보류 사유를 기반으로 폐기 기간을 연장합니다.
- **권한**: 관리자(ADMIN) 전용
- **인증**: 필수 (JWT Bearer Token)

### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| item-id | Long | ✓ | 폐기 기간을 연장할 물품 ID | 1 |

### Request Body
```json
{
  "reason_id": 1
}
```

#### Request 필드 상세

| 필드 | 타입 | 필수 | 제약조건 | 설명 | 예시 |
|------|------|------|---------|------|------|
| reason_id | Long | ✓ | NotNull | 폐기 보류 사유 ID | 1 |

#### Validation Rules
- `reason_id`: NotNull

### Response

#### 201 Created - 성공
```json
{
  "message": "페기 보류 되었습니다."
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| message | String | 성공 메시지 |

#### 400 Bad Request - 잘못된 요청
```json
{
  "message": "폐기 예정 상태의 물품만 기간을 연장할 수 있습니다."
}
```

**발생 경우**:
- 물품 상태가 `TO_BE_DISCARDED`가 아닌 경우
- 보류 사유 ID가 해당 물품의 것이 아닌 경우
- 유효하지 않은 보류 사유 ID

#### 403 Forbidden - 권한 없음
```json
{
  "message": "관리자 권한이 필요합니다."
}
```

**발생 경우**:
- 관리자가 아닌 사용자가 요청한 경우

#### 404 Not Found - 리소스 없음
```json
{
  "message": "해당 보류 사유를 찾을 수 없습니다."
}
```

**발생 경우**:
- 존재하지 않는 item-id로 요청
- 존재하지 않는 reason_id로 요청

### 비즈니스 로직
1. 물품 조회 (ItemFacade)
2. 보류 사유 조회
3. 보류 사유와 물품 매칭 검증
4. 관리자 권한 검증
5. 물품 상태 검증 (TO_BE_DISCARDED)
6. 폐기 예정일 연장 (현재 시각 + extension_days)

### 예시

#### 성공 케이스
**Request**
```bash
curl -X PATCH "http://localhost:8080/items/1/discarded" \
  -H "Authorization: Bearer [ADMIN_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "reason_id": 1
  }'
```

**Response (201)**
```json
{
  "message": "페기 보류 되었습니다."
}
```

#### 실패 케이스 - 권한 없음
**Request**
```bash
curl -X PATCH "http://localhost:8080/items/1/discarded" \
  -H "Authorization: Bearer [TEACHER_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "reason_id": 1
  }'
```

**Response (403)**
```json
{
  "message": "관리자 권한이 필요합니다."
}
```

#### 실패 케이스 - 잘못된 물품 상태
**Request**
```bash
curl -X PATCH "http://localhost:8080/items/5/discarded" \
  -H "Authorization: Bearer [ADMIN_TOKEN]" \
  -H "Content-Type: application/json" \
  -d '{
    "reason_id": 1
  }'
```

**Response (400)**
```json
{
  "message": "폐기 예정 상태의 물품만 기간을 연장할 수 있습니다."
}
```

---

## 4. 자동 폐기 관련 스케줄러

### 4-1. 폐기 예정 전환 스케줄러

#### 기본 정보
- **실행 방식**: 백그라운드 스케줄러 (자동 실행)
- **실행 주기**: 매일 자정 (00:00)
- **설명**: 장기 방치된 분실물을 폐기 예정 상태로 자동 전환합니다.

#### 스케줄 설정
```java
@Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
```

#### 처리 로직
1. 현재 시각 기준 (6개월 - 2주) 전 날짜 계산
2. LOST 상태이면서 등록일(createdAt)이 (6개월 - 2주) 이전인 물품 조회
3. 각 물품에 대해 `markAsToBeDiscarded()` 메서드 호출
   - 상태를 `TO_BE_DISCARDED`로 변경
   - 폐기 예정일을 **등록일(createdAt) + 6개월**로 설정
4. 로그 기록

#### 보관 기간 설정
```java
private static final int RETENTION_MONTHS = 6; // 6개월
private static final int GRACE_PERIOD_WEEKS = 2; // 2주
```
- 기본값: 6개월
- 유예 기간: 2주
- 변경 가능: 필요시 상수 값 수정

#### 로그 예시

**정상 실행**
```
2024-07-15 00:00:00 [INFO] 폐기 예정 전환 스케줄러 시작
2024-07-15 00:00:01 [INFO] 물품 폐기 예정으로 전환 완료 - ID: 3, 이름: 빨간색 우산, 등록일: 2024-01-10T14:30:00, 폐기 예정일: 2024-07-10T14:30:00
2024-07-15 00:00:01 [INFO] 물품 폐기 예정으로 전환 완료 - ID: 7, 이름: 회색 지갑, 등록일: 2024-01-05T09:15:00, 폐기 예정일: 2024-07-05T09:15:00
2024-07-15 00:00:02 [INFO] 폐기 예정 전환 스케줄러 완료 - 총 2개 물품 전환 (등록일 + 6개월에 폐기 예정)
```

**전환 대상 없음**
```
2024-01-15 00:00:00 [INFO] 폐기 예정 전환 스케줄러 시작
2024-01-15 00:00:00 [INFO] 폐기 예정 전환 대상 물품이 없습니다.
```

**에러 발생**
```
2024-07-15 00:00:00 [INFO] 폐기 예정 전환 스케줄러 시작
2024-07-15 00:00:01 [INFO] 물품 폐기 예정으로 전환 완료 - ID: 3, 이름: 빨간색 우산, 등록일: 2024-01-10T14:30:00, 폐기 예정일: 2024-07-10T14:30:00
2024-07-15 00:00:01 [ERROR] 물품 폐기 예정 전환 중 오류 발생 - ID: 7, 오류: 분실물 상태의 물품만 폐기 예정으로 변경할 수 있습니다.
2024-07-15 00:00:02 [INFO] 폐기 예정 전환 스케줄러 완료 - 총 1개 물품 전환 (등록일 + 6개월에 폐기 예정)
```

---

### 4-2. 자동 폐기 스케줄러

#### 기본 정보
- **실행 방식**: 백그라운드 스케줄러 (자동 실행)
- **실행 주기**: 매일 자정 (00:00)
- **설명**: 폐기 예정일이 지난 물품을 자동으로 폐기 처리합니다.

#### 스케줄 설정
```java
@Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
```

### 처리 로직
1. 현재 시각 기준 폐기 예정일이 지난 물품 조회
   - 조건: `status = TO_BE_DISCARDED` AND `discardedAt < 현재시각`
2. 각 물품에 대해 `discard()` 메서드 호출
   - 상태를 `DISCARDED`로 변경
   - 폐기일 기록 (`discardedAt`)
3. 로그 기록
   - 시작/종료 로그
   - 각 물품 폐기 완료 로그
   - 에러 발생 시 에러 로그

### 로그 예시

#### 정상 실행
```
2024-01-15 00:00:00 [INFO] 자동 폐기 스케줄러 시작
2024-01-15 00:00:01 [INFO] 물품 자동 폐기 완료 - ID: 5, 이름: 파란색 우산, 폐기 예정일: 2024-01-14T23:59:59
2024-01-15 00:00:01 [INFO] 물품 자동 폐기 완료 - ID: 12, 이름: 검은색 지갑, 폐기 예정일: 2024-01-13T10:30:00
2024-01-15 00:00:02 [INFO] 자동 폐기 스케줄러 완료 - 총 2개 물품 폐기
```

#### 폐기 대상 없음
```
2024-01-15 00:00:00 [INFO] 자동 폐기 스케줄러 시작
2024-01-15 00:00:00 [INFO] 폐기 대상 물품이 없습니다.
```

#### 에러 발생
```
2024-01-15 00:00:00 [INFO] 자동 폐기 스케줄러 시작
2024-01-15 00:00:01 [INFO] 물품 자동 폐기 완료 - ID: 5, 이름: 파란색 우산, 폐기 예정일: 2024-01-14T23:59:59
2024-01-15 00:00:01 [ERROR] 물품 폐기 중 오류 발생 - ID: 12, 오류: 폐기 예정 상태의 물품만 폐기할 수 있습니다.
2024-01-15 00:00:02 [INFO] 자동 폐기 스케줄러 완료 - 총 1개 물품 폐기
```

### 스케줄 변경 방법

#### 실행 주기 변경
```java
// 매일 오전 2시
@Scheduled(cron = "0 0 2 * * *")

// 6시간마다
@Scheduled(cron = "0 0 0/6 * * *")

// 매주 일요일 자정
@Scheduled(cron = "0 0 0 * * SUN")
```

#### Cron 표현식 형식
```
초(0-59) 분(0-59) 시(0-23) 일(1-31) 월(1-12) 요일(0-7)
```

### 트랜잭션 처리
- `@Transactional`: 전체 폐기 프로세스가 하나의 트랜잭션으로 처리
- 개별 물품 폐기 실패 시 해당 물품만 스킵하고 계속 진행
- 에러 발생 시 로그 기록 후 다음 물품 처리

---

## 전체 워크플로우

### 1. 자동 폐기 예정 전환 워크플로우
```
[물품 등록] → 상태: LOST, createdAt 기록
           ↓
[5개월 2주 경과] (6개월 - 2주)
           ↓
[스케줄러] → 매일 자정 실행
          → createdAt < 현재시각 - (6개월 - 2주) 조회
          → 상태: LOST → TO_BE_DISCARDED
          → discardedAt = createdAt + 6개월
          → 로그 기록
```

### 2. 폐기 보류 워크플로우
```
[선생님] → POST /items/{id}/disposal-reason
         → 보류 사유 제출 + 연장 일수 설정
         → discardedAt = 현재시각 + days
         → 상태: TO_BE_DISCARDED 유지
```

### 3. 관리자 승인 워크플로우
```
[관리자] → GET /items/{id}/disposal-reason
         → 보류 사유 확인
         → PATCH /items/{id}/discarded
         → 폐기 기간 추가 연장
         → discardedAt = 현재시각 + extension_days
```

### 4. 자동 폐기 워크플로우
```
[스케줄러] → 매일 자정 실행
          → discardedAt < 현재시각 조회
          → 상태: TO_BE_DISCARDED → DISCARDED
          → 로그 기록
```

---

## 데이터베이스 스키마

### items 테이블
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | 물품 ID |
| status | VARCHAR | NOT NULL | 물품 상태 (LOST, TO_BE_DISCARDED, DISCARDED, GIVEN) |
| discarded_at | DATETIME | NULL | 폐기 예정일 또는 폐기 완료일 |

### reasons 테이블 (DisposalReason)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | 보류 사유 ID |
| item_id | BIGINT | FK, NOT NULL | 물품 ID |
| teacher_id | BIGINT | FK, NOT NULL | 선생님 ID |
| reason | TEXT | NOT NULL | 보류 사유 |
| extension_days | INTEGER | NOT NULL | 연장 일수 |
| created_at | DATETIME | NOT NULL | 생성 일시 |

---

## 상태 전이도

```
LOST (분실물 등록)
  ↓ createdAt 기록
  ↓
  ↓ (5개월 2주 경과)
  ↓
TO_BE_DISCARDED (폐기 예정) ← 자동 전환 스케줄러
  ↓ discardedAt = createdAt + 6개월
  ↓
  ↓ (선생님 보류 사유 제출 - 선택)
  ↓
TO_BE_DISCARDED + discardedAt 재설정
  ↓
  ↓ (관리자 기간 연장 - 선택)
  ↓
TO_BE_DISCARDED + discardedAt 재설정
  ↓
  ↓ (6개월 도래 또는 연장 기간 만료)
  ↓
DISCARDED (폐기 완료) ← 자동 폐기 스케줄러
```

### 상태 전이 타임라인 예시

```
Day 0:   물품 등록 (LOST)
         └─ createdAt: 2024-01-01 10:00

Day 166: 자동 폐기 예정 전환 (TO_BE_DISCARDED) [5개월 2주 후]
         └─ 스케줄러 실행
         └─ discardedAt: 2024-07-01 10:00 (createdAt + 6개월)

Day 170: 선생님 보류 사유 제출 (선택)
         └─ discardedAt: 2024-07-06 10:00 (5일 연장)

Day 173: 관리자 기간 연장 (선택)
         └─ discardedAt: 2024-07-13 10:00 (7일 추가 연장)

Day 194: 자동 폐기 완료 (DISCARDED)
         └─ 스케줄러 실행
```

---

## 에러 코드 정리

| HTTP 상태 | 에러 메시지 | 발생 조건 | API |
|-----------|------------|----------|-----|
| 400 | 폐기 예정 상태의 물품만 보류할 수 있습니다. | 상태가 TO_BE_DISCARDED가 아님 | POST, PATCH |
| 400 | 보류 사유는 필수입니다. | reason 필드 누락 | POST |
| 400 | 연장 일수는 양수여야 합니다. | days가 0 이하 | POST |
| 400 | 연장 일수는 365일을 초과할 수 없습니다. | days가 365 초과 | POST |
| 400 | 해당 보류 사유는 이 물품의 것이 아닙니다. | reason_id와 item_id 불일치 | PATCH |
| 403 | 선생님 또는 관리자 권한이 필요합니다. | USER 역할로 요청 | POST |
| 403 | 관리자 권한이 필요합니다. | ADMIN이 아닌 사용자로 요청 | PATCH |
| 404 | 물품을 찾을 수 없습니다. | 존재하지 않는 item_id | ALL |
| 404 | 해당 물품의 폐기 보류 사유를 찾을 수 없습니다. | 보류 사유 미등록 | GET |
| 404 | 해당 보류 사유를 찾을 수 없습니다. | 존재하지 않는 reason_id | PATCH |

---

## 테스트 시나리오

### 시나리오 1: 전체 자동 워크플로우
1. 물품 등록 (LOST) - createdAt: 2024-01-01
2. 5개월 2주 대기 (또는 테스트용 시간 조작)
3. 폐기 예정 전환 스케줄러 실행 → TO_BE_DISCARDED, discardedAt: 2024-07-01 (createdAt + 6개월)
4. 2주 대기 (또는 테스트용 시간 조작)
5. 자동 폐기 스케줄러 실행 → DISCARDED

### 시나리오 2: 정상 워크플로우 (수동 개입)
1. 물품 등록 (LOST) - createdAt: 2024-01-01
2. 5개월 2주 경과 → 자동으로 TO_BE_DISCARDED로 변경, discardedAt: 2024-07-01
3. 선생님이 보류 사유 제출 (POST) → 폐기일 5일 연장 (2024-07-06)
4. 관리자가 보류 사유 조회 (GET)
5. 관리자가 폐기 기간 추가 연장 (PATCH) → 폐기일 재설정 (2024-07-13)
6. 스케줄러가 폐기일 도래 시 자동 폐기 → DISCARDED

### 시나리오 3: 권한 검증
1. USER 역할로 보류 사유 제출 시도 → 403
2. TEACHER 역할로 폐기 기간 연장 시도 → 403
3. ADMIN 역할로 정상 처리 → 201

### 시나리오 4: 유효성 검증
1. 연장 일수 0으로 제출 → 400
2. 연장 일수 500으로 제출 → 400
3. 보류 사유 빈 문자열로 제출 → 400
4. reason_id null로 연장 요청 → 400

### 시나리오 5: 자동 폐기 예정 전환 스케줄러
1. 5개월 2주 이상 방치된 LOST 물품 5개 생성
2. 폐기 예정 전환 스케줄러 실행
3. 5개 물품 모두 TO_BE_DISCARDED 상태로 변경 확인
4. discardedAt이 createdAt + 6개월로 설정 확인
5. 로그 기록 확인

### 시나리오 6: 자동 폐기 스케줄러
1. 폐기일이 지난 TO_BE_DISCARDED 물품 5개 생성
2. 자동 폐기 스케줄러 실행
3. 5개 물품 모두 DISCARDED 상태로 변경 확인
4. 로그 기록 확인

---

## 주의사항

### 개발 시 유의점
1. **트랜잭션 관리**: 폐기 관련 모든 작업은 `@Transactional` 적용
2. **동시성 제어**: 동일 물품에 대한 동시 요청 처리 고려
3. **타임존**: 모든 날짜/시간은 서버 시간 기준 (Asia/Seoul)
4. **로그 레벨**: 프로덕션에서는 INFO 레벨 유지
5. **보관 기간 설정**:
   - `RETENTION_MONTHS` 상수로 보관 기간 조정 (기본 6개월)
   - `GRACE_PERIOD_WEEKS` 상수로 유예 기간 조정 (기본 2주)
6. **폐기일 계산**: `markAsToBeDiscarded()` 메서드에서 createdAt + 6개월로 설정

### 운영 시 유의점
1. **스케줄러 모니터링**:
   - 폐기 예정 전환 스케줄러 로그 확인 (매일 자정)
   - 자동 폐기 스케줄러 로그 확인 (매일 자정)
2. **폐기 데이터 백업**: DISCARDED 상태로 변경 전 백업 권장
3. **권한 관리**: ADMIN 권한 부여 시 신중하게 검토
4. **알림 설정**:
   - TO_BE_DISCARDED 전환 시 관리자 알림 권장
   - 자동 폐기 전 관리자에게 알림 발송 고려
5. **보관 기간 정책**: 법적 요구사항에 따라 보관 기간 조정 필요

---

## 버전 정보
- **작성일**: 2024-01-15
- **최종 수정일**: 2026-01-07
- **버전**: 1.3
- **Spring Boot**: 3.5.4
- **Java**: 17

## 변경 이력

### v1.3 (2026-01-07)
- **폐기일 기준 변경**: 습득일이 아닌 등록일(createdAt) 기준으로 폐기 예정일 계산
- **ItemSchedulerService**: 등록일 기준 조회 로직으로 변경
- **ItemRepository**: `findByStatusAndCreatedAtBefore()` 쿼리 메서드로 변경
- **문서 업데이트**: 로그, 워크플로우, 테스트 시나리오 기준 날짜 갱신

### v1.2 (2024-01-15)
- **중요 변경**: 보관 기간을 3개월에서 **6개월**로 변경
  - 습득일로부터 6개월 후 자동 폐기
  - 5개월 2주 시점에 TO_BE_DISCARDED로 자동 전환
- **폐기일 계산 방식 변경**:
  - 기존: 전환 시점 + 2주
  - 변경: foundAt + 6개월 (고정)
- **ItemSchedulerService**:
  - `RETENTION_MONTHS` 상수: 3 → 6
  - `GRACE_PERIOD_WEEKS` 상수 추가: 2주
  - 전환 시점 계산 로직 변경
- **Item 엔티티**: `markAsToBeDiscarded()` 메서드 폐기일 계산 방식 변경
- **문서 업데이트**: 전체 워크플로우, 타임라인, 테스트 시나리오 갱신

### v1.1 (2024-01-15)
- **신규 기능**: 자동 폐기 예정 전환 스케줄러 추가
  - 습득일로부터 3개월 경과 시 자동으로 TO_BE_DISCARDED 전환
  - 전환 시 2주 후 폐기 예정일 자동 설정
- **Item 엔티티**: `markAsToBeDiscarded()` 메서드 추가
- **ItemRepository**: `findByStatusAndFoundAtBefore()` 쿼리 메서드 추가
- **ItemSchedulerService**: `autoMarkItemsAsToBeDiscarded()` 스케줄러 메서드 추가
- **문서 업데이트**: 전체 워크플로우, 상태 전이도, 테스트 시나리오 갱신

### v1.0 (2024-01-15)
- 초기 버전: 폐기 관련 API 명세서 작성
- 폐기 보류 사유 제출/조회/기간 연장 API
- 자동 폐기 스케줄러
