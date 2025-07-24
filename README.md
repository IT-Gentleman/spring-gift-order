# spring-gift-order

## Step0
<details>
<summary>Click to view details</summary>

### implementation
#### [feat]
- import previous features
  - CRUD of product
    - [api/thymeleaf] Create, Read, Update, Delete(Soft Delete)
    - 상품명 유효성 검사는 Entity에서 수행, 유효하지 않을 시 `validated=false`
    - 상품명 validated 값 변환은 Admin Page에서만 처리 가능
  - CRUD of member
    - [api] Create(role=`Role.ROLE_USER`로 고정)
    - [thymeleaf] Create, Read, Update, Delete(Soft Delete)
  - CR_D of wish
    - [api] Create, Read, Delete
  - Authentication / Authorization
    - tokenization 활용 인증
    - API.md에 명시된 권한에 따라 각 요청에 대해 인가
  - JPA 적용
    - 전 엔티티 JDBC -> JPA 기반으로 변경
    - JPA Auditing 적용
      - ThreadLocal 활용 유저정보 저장 및 활용
      - 기존 Soft Delete를 `deleted` 컬럼으로 수행하던 방법을 `deletedAt`의 nullable 검사로 대체
</details>

## Step1
### implementation
#### [refactor]
- [x] Entity 필드의 `Min`, `Max` 어노테이션 제거
  - flush 시점에 어노테이션이 검증되기는 하나, set시점에 명시적으로 검증하도록 변경
- [x] ProductOptionRepository의 미사용 메소드 제거
- [x] JPA Auditing 과정에서 `Member` 객체를 직접 Auditor로 지정하지 않고, memberId(Long)를 사용하도록 함
  - 위험성(순환참조 등), 복잡성, 의존성 낮추기 위한 조치
- [x] LoginMemberArgumentResolver에서 ThreadLocal 활용한 id 추출
  - JPA Auditing 과정에서 추출한 memberId를 AuthService에 전달하여, 검증과정을 생략하고 곧바로 Member 객체 조회
- [x] ThreadLocal set/clear 부의 try-finally로 감싸기
- [x] HardDeleteEntity 삭제
  - 불필요한 복잡성 감소 (현재 HardDeleteEntity는 역할 없음)
  - 해당 엔티티를 상속한 엔티티들은 BaseAuditingEntity를 직접 상속하도록 변경

#### [feat]
- [x] 카카오 로그인 구현
- [x] 카카오 로그인(Auth) 간 에러발생 Handling 구현

#### [chore]
- [x] Controller와 Dto 내부 패키지 분리
  - 가독성 향상 목적

## TODO
### Whenever is ready
#### [feat]
- 상품 옵션 관리 Admin Page 구현
  - 현행 '기존 상품'은 상품 옵션을 표시하지 않도록 구현됨
- `Role.ROLE_SELLER`에 한해 본인 등록 상품 수정 기능 추가
- 상품 옵션 재고 동시수정 시 동시성 문제 해결
  - `Version` 어노테이션을 가진 필드 추가를 통한 낙관적 Locking이 가능해보임
  - 이 구현방법 사용 시, Service 레이어에서 최대 N회 재시도하는 로직 구현 필요
- Entity 단위테스트 추가
- Kakao Auth 요청/응답확인 간 state 사용을 통한 CSRF 방지

### Wondering to apply
#### [refactor]
- Service 레이어 중 Admin(Page)에게만 허용된 로직의 별도 레이어 분리
- sortProperty의 enum화