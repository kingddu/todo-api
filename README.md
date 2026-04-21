# TodoKing API

TodoKing의 백엔드 API 서버입니다.  
사용자 인증, 할 일 관리, 그룹 기능, 프로필 관리 등을 처리하는 Spring Boot 기반 애플리케이션입니다.

---

## 📌 프로젝트 소개

TodoKing API는 TodoKing 서비스의 백엔드 서버입니다.

이 서버는 다음과 같은 기능을 담당합니다:

- 회원가입 / 로그인 / 로그아웃
- 이메일 인증 및 비밀번호 재설정
- 사용자 정보 조회 및 수정
- 오늘 / 예정 / 기록 기반 할 일 조회 및 관리
- 그룹 생성 / 초대 / 수락 / 거절 / 차단
- 프로필 이미지 업로드
- 달성률 조회
- 프론트엔드와의 인증/세션 연동

---

## 🛠 기술 스택

- **Language**
    - Java 17

- **Framework**
    - Spring Boot

- **Database**
    - MariaDB
    - JPA (Spring Data JPA)

- **Security**
    - Spring Security
    - Redis Session

- **Etc**
    - Spring Validation
    - Spring Mail
    - Swagger / OpenAPI
    - Thumbnailator (이미지 처리)
    - Lombok

---

## 📁 프로젝트 구조

```bash
todo-api/
├─ src/
├─ gradle/
├─ build.gradle
├─ settings.gradle
├─ gradlew
├─ gradlew.bat
└─ README.md