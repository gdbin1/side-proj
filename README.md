**Side-Proj**

**프로젝트 개요**:
- **설명**: 공공 입찰·웹 아이템 조회를 중심으로 한 Spring Boot 기반 웹 애플리케이션입니다. 온비드(또는 외부 API)를 연동하여 물품 조회, 찜(Favorite), 입찰·관심 목록, 사용자 관리 기능을 제공합니다.
- **목적**: 개인 포트폴리오 및 이력서용 데모 프로젝트로 사용됩니다. 실제 API 연동과 DB 기반 CRUD 흐름을 보여주기 위해 설계되었습니다.

**기술 스택**:
- **프레임워크**: Spring Boot (버전: 3.5.7) — [build.gradle](build.gradle)
- **템플릿**: Thymeleaf (서버 렌더링 UI)
- **ORM/Mapper**: MyBatis (mybatis-spring-boot-starter)
- **DB**: MariaDB (JDBC: mariadb-java-client)
- **보안**: Spring Security
- **API 문서/도구**: springdoc-openapi
- **유틸/라이브러리**: Lombok, Jackson(core + xml), Apache HttpClient, Thymeleaf extras (java8time)

**프로젝트 구조(요약)**:
- `src/main/java/com/gbk/sideproj/controller` : 페이지 렌더링 및 API 엔드포인트를 담당하는 컨트롤러들 (`PageController`, `WebItemController`, `WebItemApiController` 등)
- `src/main/resources/templates` : Thymeleaf 템플릿들 (`main.html`, `items.html`, `login.html`, `signup.html`, `bid.html`, `favoriteList.html` 등)
- `src/main/resources/mapper` : MyBatis XML 매퍼 파일들 (`WebItemMapper.xml`, `UserMapper.xml`, `BidMapper.xml` 등)
- `src/main/java/com/gbk/sideproj/service` : 비즈니스 로직 레이어
- `src/main/java/com/gbk/sideproj/domain` : 도메인(VO/DTO) 클래스들

**주요 기능**:
- 공공 API 연동을 통한 아이템(물품) 목록 조회 및 상세보기
- 아이템 찜(Favorite) 추가/삭제 및 찜 목록 조회
- 입찰 내역 페이지 및 입찰 관련 기능(입찰 등록/보기)
- 사용자 회원가입·로그인(스프링 시큐리티를 통한 인증 처리)
- 관리자 페이지(기본적인 관리 인터페이스)
- REST API: `WebItemApiController` 등으로 내부/외부 연동에 사용 가능한 JSON 엔드포인트 제공

**설치 및 실행 방법**:
1. 요구사항
   - JDK 21
   - MariaDB (또는 호환되는 MySQL 계열 DB)

2. DB 설정
   - `src/main/resources/application.properties`의 `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`를 로컬 환경에 맞게 변경합니다.
   - MyBatis 매퍼들은 `src/main/resources/mapper`에 위치합니다.

3. 환경변수
   - 외부 API 키: `api.serviceKey` (환경변수 또는 `application.properties`에 설정)

4. 빌드·실행
   - 윈도우 (프로젝트 루트에서):

```bash
./gradlew bootRun
```

   - 또는 빌드 후 실행:

```bash
./gradlew clean bootJar
java -jar build/libs/side-proj-0.0.1-SNAPSHOT.jar
```

**개발자 참고(파일/핵심 클래스)**:
- 빌드 설정: [build.gradle](build.gradle)
- 애플리케이션 진입점: `src/main/java/com/gbk/sideproj/SideProjApplication.java`
- 템플릿 위치: [src/main/resources/templates](src/main/resources/templates)
- 매퍼 위치: [src/main/resources/mapper](src/main/resources/mapper)
- 주요 컨트롤러: `controller` 패키지 내 `WebItemController`, `WebItemApiController`, `UserController`, `FavoriteController`, `BidController`, `ContactController`, `AdminController`

**테스트**:
- `./gradlew test`로 단위/통합 테스트를 실행할 수 있습니다. (프로젝트에 테스트 케이스가 추가되어 있는 경우)

**운영·배포 노트**:
- 외부 API 호출 시 `api.serviceKey` 보호 필요
- DB 접속 정보는 환경변수/시크릿 매니저로 관리 권장

**이력서에 쓸 요약(복사용)**:

---

프로젝트에 추가적으로 강조하고 싶은 부분(예: 성능 튜닝, 테스트 커버리지, CI/CD 설정)을 알려주시면 README에 반영해 드리겠습니다.
# side-proj



