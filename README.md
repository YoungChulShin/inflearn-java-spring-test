# 저장소 설명
인프린 `Java/Spring 테스트를 추가하고 싶은 개발자들의 오답노트` 강의 저장소

# 정리
## 개발자의 고민
무의미한 테스트
- 커버리지를 위한 테스트를 해야할까?
- 예: JPA에서 save 테스트

테스트가 불가한 코드
- 강제로 mock library를 이용해서 테스트를 주입할 수도 있지만, __설계의 문제__ 를 고려해봐야한다.

## 테스트의 필요성
1. 시간이 지날 수록 기능 개발의 시간이 늘어난다.
   - 회귀버그 방지
2. 좋은 아키텍처를 유도한다.

테스를 회귀버그 방지로만 보면 테스트의 가치가 상대적으로 떨어질 수 있다. 

## 테스트 3분류
소형 테스트 -> 전체의 80%
- 단일 서버
- 단일 프로세스
- 디스크 I/O 있으면 안된다
- Blocking call 허용 안된다 (예: Thread.sleep())

중형 테스트
- 단일 서버
- 멀티 프로세스
- 멀티 스레드
- h2 같은 것을 사용할 수 있다.

대형 테스트
- end to end 테스트

## 테스트에 필요한 개념
SUT
- system under test. 테스트 대상 

BDD
- Behavior driven development
- given / when / then (Arrange / Act / Assert)
- 행동에 집중한다.

테스트 픽스처
- 테스트에 필요한 자원을 미리 생성해두는것 

비욘세 규칙
- 유지하고 싶은 정책이 있다면 알아서 테스트를 만들어야 한다. 
- 이런것들이 모여서 시스템의 정책이 된다.

Testability
- 테스트 가능한 구조인가

test double
- 가짜 객체
   - Dummy: 아무런 동작도 하지 않고 그저 코드가 잘 돌아가도록 하는 객체
   - Fake: Local에서 사용하거나 테스트에서 사용하기 위해 만들어진 가짜 객체. 자체적인 로직이 있다.
   - Stub: 미리 준비한 값을 출력하는 객체. 보통 mockito를 이용해서 사용.
   - Mock: 메서드 호출을 확인하기 위한 객체. 
   - Spy: 메서드 호출을 전부 기록했다가 나중에 확인하기 위한 객체. 

## 의존성
### 의존성
개념
- A가 B를 사용하는 개념.
- 의존성 주입으로 약화시킬 수는 있지만 제거할 수는 없다. 

### 의존성 역전
DI와 DIP는 다르다. 

개념
- 상위 모듈은 하위 모듈에 의존해서는 안된다. 
- 추상화는 세부 사항에 의존해서는 안된다. 세부사항이 추상화에 의존해야 한다. 

### Testability
테스트를 하다가 mock 프레임워크 없이는 테스트가 불가능하다는 생각이 들면? 테스트가 보내는 설계 신호.

숨겨진 의존성은 테스트를 힘들게 만든다. 하지만 어딘가에서는 하드코딩이 들어가야한다. 

__이럴 때 의존성 역전을 고려해볼 수 있다.__

예시
```java
// 현재 시간을 가져오는 예시

// 방법 1 - 내부 구현
public void login() {
   this.lastLoginTimestamp = Clock.systemUTC().millis();
}

// 방법 2 - 의존성 주입
// 이 메서드를 호출하는 곳에서 또 구현이 필요하다.
public void login(Clock clock) {
   this.lastLoginTimestamp = clock.systemUTC().millis();
}

// 방법 3 - 의존성 역전
public void loing(ClockHolder clockHolder) {
   this.lastLoginTimestamp = clockHolder.getMillis();
}

public interface ClockHolder {
   long getMillis();
}

public class SystemClockHolder implemnets ClockHodler {
   @Override
   public long getMillis() {
      return Clock.systemUTC().millis();
   }
}
```

# 실습 메모
## DataJpaTest
`ExtendWith(SpringExtension.claa)` 를 내장하고 있다. 

`@TestPropertySource("classpath:test-application-properties")`를 이용해서 명시적으로 설정파일 위치를 지정해 줄 수 있다. 

## Repisitory 초기화
`@Sql("/sql/user-repository-test-data.sql")`과 같이 `@Sql` 애노테이션을 이용해서 초기 데이터를 설정해줄 수 있다. 

'@SQL'은 여러 메서드에서 같이 사용될 경우 충돌이 발생할 수 있는데, 이때 `@SqlGroup`으로 Sql을 묶어주고 `ExecutionPhase`를 지정해줄 수 있다. 
```java
@SqlGroup({
    @Sql(
        value = "/sql/user-service-test-data.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(
        value = "/sql/delete-all-data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
```

## 테스트 의존성 주입
`@MockBean` 애노테이션
- 테스트를 실행할 때 MockBean이 주입되어서 실행된다. 

## AssertJ
`assertThatThrownBy` 
- 예외가 발생하는 테스트에 대해서 Exception을 가져와서 처리할 수 있다. 
   ```java
   Assertions.assertThatThrownBy(() -> {
      userService.verifyEmail(2, "bbbbbbbbba");
    }).isInstanceOf(CertificationCodeNotMatchedException.class);
   ```

## MockMvcTEst
애노테이션 정의
- `@AutoConfigureMockMvc`: MockMvc를 사용하기 위한 애노테이션
- `@AutoConfigureTestDatabase`: TestDB를 사용하기 위한 애노테이션
   - replace 값을 이용해서 설정을 조정할 수 있다. 

MockMvc 가져오기
```java
@Autowired
private MockMvc mockMvc;
```

perform 수행
- 특정 값 가져오기
   - `jsonPath.value`를 이용해서 가능하다. 
- 특정 값이 없어야하는 테스트
   - `jsonPath.doesNotExist`를 이용해서 가능하다. 
- 샘플 코드
   ```java
   mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("go1323@test.com"))
        .andExpect(jsonPath("$.nickname").value("go1323"))
        .andExpect(jsonPath("$.address").doesNotExist())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
   ```

## Mockito 
BDD 스타일 테스트
```java
// BDDMockito를 사용
BDDMockito.doNothing().when(mailSender).send(any(SimpleMailMessage.class));
```

