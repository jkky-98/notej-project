# notej-project

# 목차
- [개발 환경](#개발-환경)
- [사용 기술](#사용-기술)
    * [백엔드](#백엔드)
    * [프론트엔드](#프론트엔드)
    * [데이터베이스](#데이터베이스)
    * [인프라](#인프라)
    * [기타 라이브러리](#기타-라이브러리)
- [핵심 키워드](#핵심-키워드)
- [시스템 아키텍처](#시스템-아키텍처)
- [E-R Diagram](#e-r-diagram)
- [프로젝트 목적](#프로젝트-목적)
    * [기획 의도](#기획-의도)
- [핵심 기능 Before 릴리즈](#핵심-기능-before-릴리즈)
    * [게시글 CRUD](#게시글-crud)
    * [인증 및 인가 세션방식 구현](#인증-및-인가-세션방식-구현)
    * [마크다운 작성을 위한 Toast Ui Editor 적용](#마크다운-작성을-위한-toast-ui-editor-적용)
    * [태그 및 키워드 검색 기능](#태그-및-키워드-검색-기능)
    * [작성 자동저장 가눙](#작성-자동저장-기능)
    * [알림 기능](#알림-기능)
    * [좋아요 및 팔로우 기능](#좋아요-및-팔로우-기능)
    * [무한 스크롤 페이징 기능](#무한-스크롤-페이징-기능)
    * [댓글 대댓글 기능](#댓글-대댓글-기능)
    * [자바8 스트림 람다 문법 도입](#자바8-스트림-람다-문법-도입)
    * [사진 저장용량 최적화](#사진-저장용량-최적화)
    * [Ehcache 적용](#ehcache-적용)
    * [조회수 증가 로직](#조회수-증가-로직)
    * [MapStruct 도입](#mapstruct-도입)
## 개발 환경
- IntelliJ
- GitHub

## 사용 기술
### 백엔드
#### 주요 프레임워크 / 라이브러리
- Java 17 openjdk
- SpringBoot 3.4.0
- Spring Data JPA
- QueryDSL 5.0.0
- Ehcache 3.10.8

#### 빌드 Tool
- Gradle

#### 프론트엔드
- Javascript
- Html/css
- Thymeleaf
- BootStrap5

#### 데이터베이스
- H2(dev)
- Mysql(prod)

#### 인프라
- AWS EC2
- AWS S3
- GitHub Actions
- Docker
- AWS RDS
- AWS Route53

### 기타 라이브러리
- Lombok
- Toast Ui Editor

## 핵심 키워드
- 스프링 부트를 사용하여 웹 애플리케이션 설계 ~ 배포 유지 보수까지 전 과정 개발 및 운영
- AWS / ubuntu 기반 Github action을 활용한 무중단 배포 및 배포 자동화 구축
- MVC 프레임워크 기반 백엔드 서버 구축

## 시스템 아키텍처
![noteJ-system-architecture drawio](https://github.com/user-attachments/assets/4f3a8d30-a8cb-4721-ad5b-7bd20c578989)

### 패키징 구조 예시
| 패키지명             | 설명 |
|----------------------|-------------------------------|
| `NoteJApplication.java` | Spring Boot 애플리케이션 메인 실행 클래스 |
| `aop`               | AOP(Aspect-Oriented Programming) 관련 코드 |
| `deploy`            | 배포 관련 스크립트 및 설정 파일 |
| `domain`            | 엔티티(Entity) 및 도메인 모델 관련 클래스 |
| `exception`         | 전역 예외 처리 및 커스텀 예외 클래스 |
| `file`              | 파일 업로드/다운로드 관련 처리 |
| `filter`            | 인증 인가 필터 |
| `repository`        | 데이터베이스 접근 계층 (JPA) |
| `service`           | 비즈니스 로직을 담당하는 서비스 계층 |
| `web`               | 컨트롤러 및 API 요청 처리 관련 클래스 |
## E-R Diagram

## 프로젝트 목적

### 기획 의도
이 프로젝트는 개발자들을 위한 최적화된 블로그 플랫폼을 구현하고자 시작되었습니다.  
개발자들이 자신의 지식과 경험을 자유롭게 공유하고, 최신 기술 트렌드와 노하우를 소통할 수 있는 공간을 제공하는 것을 목표로 하며 경쟁적인 요소를 추가하고자 합니다.

많은 블로그들에서 양산형으로 나오는 글들은 검색엔진에서의 우리들의 경험을 오염시키는 경우가 많습니다.
noteJ는 글에 대한 평가 요소를 다양하게 남길 수 있도록 하여 작성자들의 동기를 불러일으키려고 합니다. 

## 핵심 기능 Before 릴리즈

### 게시글 CRUD
기본적인 게시글 CRUD가 가능합니다.
Read의 경우 로그인이 필요없으며, Update, Create, Delete에 대해서는 로그인이 필요합니다.

### 인증 및 인가 세션방식 구현
Tomcat이 제공하는 HttpSession 객체를 이용하여 로그인 시 세션에 User를 업데이트하고 JSESSIONID로 하여금 인가를 판단했습니다.

로그인 세션의 만료 시간을 30분으로 두어 쿠키 탈취에 대한 보안을 강화하였습니다.

User엔티티에 잠금 관련 필드를 추가하여 로그인시 아이디는 맞지만 비밀번호가 틀리는 경우에 대하여 시도 5회 초과시 계정을 잠그고 일정 시간 이후 이가 자동적으로 풀리는 인증 처리 제한 로직을 추가하였습니다.


### 마크다운 작성을 위한 Toast Ui Editor 적용

개발자 블로그답게 게시글을 마크다운으로 작성할 수 있도록 **`Toast Ui Editor`** 오픈 소스를 사용했습니다. 

![image](https://github.com/user-attachments/assets/84553641-c871-4c0e-9ab8-a5d21c33b109)
글 작성시 첨부하는 이미지와 게시글의 썸네일 이미지의 경우에는 S3에 저장되도록 하였습니다.(사진파일 저장의 생명주기를 관리하여 저장부하를 줄이도록 하였습니다.)

작성시 js async를 사용하여 비동기적으로 blob사진을 업로드 컨트롤러에 보내어 즉각적으로 저장하고 저장된 파일명을 받아 다시 다운로드 컨트롤러로 하여금 Viewer에 해당 사진을 랜더링하도록
구현하였습니다.

생성된 글을 조회할 때에는 viewer 기능만 사용하도록 하였으며 이에 더해 tocbot.js를 활용하여 글에 대한 동적인 탭 기능을 채택하였습니다.

### 태그 및 키워드 검색 기능

태그, 키워드, 시리즈중 하나로 검색이 가능하며 동적인 쿼리를 다루기 위해 QueryDSL을 기반으로한 데이터 조회 로직을 구현했습니다.

태그의 생성은 글 작성시 이루어지며 tagging.js를 활용하여 태그 작성시 중복을 화면단에서 동적으로 제거하고, 엔터로 하여금 태그를 손쉽게 생성할 수 있도록 하였습니다.

### 작성 자동저장 기능

게시글에 대해 writable 필드를 구분자로 하여금 임시저장글과 공개글을 구분하고, 

작성 시 지속적으로 임시저장 글에 자동 저장을 하도록 ajax로 하여금 비동기 통신으로 구현하였습니다.

첫 작성과 수정에 대한 동기화를 위해 첫 작성 자동 저장 성공시 수정 템플릿으로 리다이렉트하여 첫 작성시 자동저장 기능과 수정에 대한 자동저장 기능을 구분해서 사용하였습니다.

구현에 대한 작성글은 아래에서 확인하시길 바랍니다.
[게시글 자동저장 기능 구현 포스팅](https://velog.io/@aal2525/%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%9E%90%EB%8F%99-%EC%A0%80%EC%9E%A5-%EA%B8%B0%EB%8A%A5-%EA%B5%AC%ED%98%84)

### 알림 기능

타 블로그 유저가 이용자의 게시글에 댓글을 남기거나 이용자의 게시글에 좋아요를 누르거나 이용자의 블로그 자체를 팔로우 할 수 있습니다.

해당 기능을 수행할 시 이용자는 이에 대한 알림을 받아야 합니다.

해당 기능을 수행할 시 알림 엔티티를 생성하여 알림수신자가 이를 조회할 수 있게 만들었습니다.

![image](https://github.com/user-attachments/assets/b1219a0f-5aa6-4220-95ea-c43ea27f2067)
네비게이션에서 읽지 않은 알림이 존재할 경우 빨간 숫자로 랜더링하여 이를 쉽게 알아볼 수 있도록 하였습니다.

알림을 누를 경우 알림관리 페이지로 들어갈 수 있습니다.

![image](https://github.com/user-attachments/assets/889a19eb-4bca-4111-84ed-e1091b973134)
알림페이지에서는 클릭 가능한 알림카드들이 존재하며 읽지 않았을 경우 흰색으로 읽었을 경우 회색으로 표시됩니다.

알림을 단건 클릭할 경우 읽은 것으로 처리됩니다.

(읽을 경우 네비게이션 바 알림의 수에 더 이상 포함되지 않습니다.)

알림 `모두 읽기`, `모두 삭제`를 통해 알림에 대한 처리가 가능합니다.

읽지않은 알림만 볼 수 있도록 필터 기능을 넣어두었습니다.

### 좋아요 및 팔로우 기능

좋아요 및 팔로우 기능으로 하여금 게시글의 좋아요에 대한 출처 유저를 볼 수 있거나 유저들의 팔로우, 팔로잉 목록을 볼 수 있도록 하였습니다.

자신이 누른 좋아요 게시글만 모아볼 수 있도록 Personal 좋아요 리스트 페이지를 추가하였습니다.

### 무한 스크롤 페이징 기능

게시글 리스트 렌더링에 Pageable을 활용한 페이징 기술을 적용하였습니다.

초기 로드 시 5개의 게시글 카드를 렌더링하며, AJAX 기반의 무한 스크롤 기능을 구현하여 사용자가 스크롤을 최하단으로 내릴 경우 추가로 5개의 게시글 카드를 가져오도록 구성하였습니다.

또한, 쿼리 스트링을 통해 tagName, seriesName, search와 같은 조건별 조회 기능을 지원하며, 이를 Querydsl을 활용한 동적 조회 쿼리로 처리하여 효율적인 조건별 데이터 조회가 가능하도록 구현하였습니다.
#### 관련 작성글
[무한 스크롤 페이징 기능 구현 과정](https://velog.io/write?id=965f2736-9586-413c-812f-c9a523e860d6)

### 댓글 대댓글 기능

Thymeleaf의 템플릿 조각(th:fragment)을 활용하여 계층적인 댓글(대댓글 포함)을 재귀적으로 렌더링하는 구조로 설계되었습니다.

자기 자신을 연관관계로 맺는 parentId와 childrens를 이용하여 댓글과 대댓글을 논리적 계층 구조로 영속화 후, 반환을 위한 DTO작업에서 children이 존재할 경우 무한으로 부모CommentsDto가 자식CommentsDto를 가지도록 만들었습니다.

템플릿에서는 th:block 및 th:replace를 사용하여 commentBlock을 재귀적으로 호출함으로써 무한 대댓글 구조를 처리할 수 있도록 구현하였습니다.

또한, collapse 기능을 활용한 대댓글 입력 폼을 제공하여 사용자 경험을 개선하며, 로그인 상태에 따라 댓글 작성 UI를 제어하여 접근성을 보장합니다.

<img width="901" alt="대댓글" src="https://github.com/user-attachments/assets/d3e112bd-3078-44d2-aa69-88f682a60567" />


### 자바8 스트림 람다 문법 도입

스트림, 람다를 학습하여 이를 프로젝트에 적용하였습니다. 적용하지 않은 코드가 존재하는데 이는 가독성을 기준으로 개발자 본인의 판단하에 결정하였습니다.

[람다 적용 커밋](https://github.com/jkky-98/notej-project/commit/787e273dc97936401a1091940344218329c3d647)

### 사진 저장용량 최적화

기존 Editor 작업에서 사진을 첨부할 때, 즉각적으로 저장소에 업로드 되어 실제 저장시 해당 사진들을 사용하지 않을 경우 저장소에 쓰지 않는 사진들이 쌓이는 경우가 발생하였습니다. 

이를 S3 객체 태그를 이용한 생명주기 관리를 통해 실제 게시글 저장 시점에 content에서 쓰이는 사진 객체에 대해 영속화 태그를 부착하고, 작성 시점 업로드된 태그에는 임시태그를 부착했습니다. 

태그의 상태관리는 PostFile이라는 엔티티를 통해 관리하여 수정시 새로운 영속화 태그를 부착하거나 기존 영속화 태그를 임시태그로 변환하는 등의 작업이 가능했습니다.

#### 관련 작성글

[사진 저장용량 최적화 구현 과정](https://velog.io/@aal2525/Toast-Editor-%EC%82%AC%EC%A7%84-%EC%A0%80%EC%9E%A5-%EC%B5%9C%EC%A0%81%ED%99%94)

#### 악성 사용자 막기

다음과 같은 극단적인 악성 사용자 시나리오를 방지하고자 짧은 시간안에 에디터 이미지 업로드가 매우 자주 들어왔을 경우 해당 컨트롤러의 접근을 1분간 막도록 구현했습니다.

요청 수를 카운트하고 상태를 안전하게 관리하기 위해, 동시성 관점에서 안전한 캐시를 제공하는 Google의 오픈소스 라이브러리인 Guava를 사용하여 상태 관리를 구현하였습니다. Guava는 동시성 제어와 자동 만료 기능을 내장하고 있어, 복잡한 로직 없이 효율적으로 요청 제한 로직을 처리할 수 있습니다.

지정한 시간동안 최대 요청수를 인자로 받는 RateLimit AOP를 설계하여 해당 기능을 다른 컨트롤러 메서드에서도 활용할 수 있게 하였습니다.

(아래의 테스트를 위한 영상으로, 1분간 5회 초과 요청이 들어올 경우 alert를 띄우는 것으로 기능을 만들었습니다. 실제 적용시에는 더욱 관대하게 적용할 것입니다.)
![화면 기록 2025-01-21 오후 4 52 14](https://github.com/user-attachments/assets/ba1dd182-5e63-439b-bffb-a5a87413510a)

#### 악성 사용자 막기 V2

기존 AOP 방식의 RateLimit(처리율 제한) 기능을 필터로 옮겨 재 구현하였습니다.

관련 작성글 : [처리율 제한 장치 AOP -> Filter](https://velog.io/@aal2525/%EC%B2%98%EB%A6%AC%EC%9C%A8-%EC%A0%9C%ED%95%9C%EC%97%90%EC%84%9C-Spring-AOP-vs-Filter-%EB%8B%B9%EC%8B%A0%EC%9D%98-%EC%84%A0%ED%83%9D%EC%9D%80)

### Ehcache 적용

웹 애플리케이션에서 태그 목록이나 네비게이션과 같은 반복적으로 동일한 데이터를 조회하는 경우, 매번 DB에 쿼리를 실행하면 성능 저하와 불필요한 리소스 소모가 발생할 수 있습니다. 

이를 해결하기 위해 Ehcache를 활용하여 캐싱을 적용하였습니다.

Ehcache를 통해 변경이 적고, 조회 부담이 큰 데이터를 캐싱하여 불필요한 데이터베이스 조회를 최소화하였으며, 캐시의 Key-Value 구조를 활용하여 다양한 메서드에서 캐시 데이터를 효과적으로 관리할 수 있도록 구성하였습니다.

또한, LRU(Least Recently Used) 알고리즘을 적용하여 가장 오래된 데이터부터 폐기하도록 설정하였으며, TTL(Time-To-Live)을 6시간으로 지정하여 일정 시간이 지나면 자동으로 캐시가 갱신되도록 설정하였습니다. 

이를 통해 데이터 일관성을 유지하면서도 성능 최적화를 달성할 수 있도록 하였습니다.

### 조회수 증가 로직
조회수 증가의 경우 게시글 엔티티에 viewCount를 추가하여 이를 increase하는 방식으로 선택하였습니다.

쿠키와 Ehcache를 이용하여 조회수 증가 중복을 막고 낙관적 락을 이용하여 동시성 문제를 최소화하면서도 성능은 크게 떨어지지 않도록 하였습니다.

조회수 증가 로직 구현 작성 글 : [조회수 증가 로직 구현](https://velog.io/@aal2525/%EC%BF%A0%ED%82%A4-%EC%9D%B8%EB%A9%94%EB%AA%A8%EB%A6%AC-%EC%BA%90%EC%8B%9C-%EA%B8%B0%EB%B0%98-%EA%B2%8C%EC%8B%9C%EA%B8%80-%EC%A1%B0%ED%9A%8C%EC%88%98-%EB%A1%9C%EC%A7%81)

### MapStruct 도입
데이터 객체와 엔티티 객체간의 변환을 위해 MapStruct, ModelMapper중 기술 적용 여부를 위한 사전조사[사전조사 관련 글](https://velog.io/@aal2525/%EA%B0%9D%EC%B2%B4-%EB%A7%A4%ED%95%91%EC%9D%98-%EC%B5%9C%EC%A0%81-%EC%86%94%EB%A3%A8%EC%85%98-MapStruct-vs-ModelMapper-%EB%B9%84%EA%B5%90-%EB%B6%84%EC%84%9D)
 후 MapStruct를 선택했습니다.

성능적으로 리플렉션 방식의 ModelMapper보다 훌륭한 퍼포먼스를 보여주었고 사용하는데 있어 어려운 느낌은 크게 없었다고 생각해서 MapStruct를 선택했습니다. 

엔티티나 DTO의 생성로직을 한 영역으로 몰아 처리할 수 있었으며 List 변환과 같이 자동적인 부분에서 오는 코드량 감소의 효과가 있었습니다.