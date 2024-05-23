package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.example.demo.exception.CertificationCodeNotMatchedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.UserStatus;
import com.example.demo.model.dto.UserCreateDto;
import com.example.demo.model.dto.UserUpdateDto;
import com.example.demo.repository.UserEntity;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@SqlGroup({
    @Sql(
        value = "/sql/user-service-test-data.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(
        value = "/sql/delete-all-data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})

class UserServiceTest {

  @Autowired
  private UserService userService;

  // 테스트를 실행할 때 MockBean이 주입되어서 실행된다.
  @MockBean
  private JavaMailSender mailSender;

  @Autowired
  private UserRepository userRepository;

  @Test
  void getByEmail_은_ACTIVE_상태인_유저를_찾아올_수_있다() {
    // given
    String email = "go1323@test.com";

    // when
    UserEntity result = userService.getByEmail(email);

    // then
    Assertions.assertThat(result.getNickname()).isEqualTo("go1323");
  }

  @Test
  void getByEmail_은_PENDING_상태인_유저는_찾아올_수_없다() {
    // given
    String email = "go13231@test.com";

    // when
    // then
    Assertions.assertThatThrownBy(() -> userService.getByEmail(email))
            .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void UserCreateDto_를_이용해서_유저를_생성할_수_있다() {
    // given
    UserCreateDto userCreateDto = UserCreateDto.builder()
        .email("test@test.com")
        .address("Inchon")
        .nickname("test")
        .build();

    BDDMockito.doNothing()
        .when(mailSender)
        .send(any(SimpleMailMessage.class));


    // when
    UserEntity result = userService.create(userCreateDto);

    // then
    Assertions.assertThat(result).isNotNull();
    Assertions.assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
  }

  @Test
  void UserUpdateDto_를_이용해서_유저를_수정할_수_있다() {
    // given
    UserUpdateDto updateDto = UserUpdateDto.builder()
        .address("Seoul")
        .nickname("test2")
        .build();


    // when
    userService.update(1, updateDto);

    // then
    UserEntity userEntity = userService.getById(1L);
    Assertions.assertThat(userEntity).isNotNull();
    Assertions.assertThat(userEntity.getAddress()).isEqualTo("Seoul");
    Assertions.assertThat(userEntity.getNickname()).isEqualTo("test2");
  }

  @Test
  void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
    // given
    // when
    userService.login(1L);

    // then
    UserEntity userEntity = userService.getById(1L);
    Assertions.assertThat(userEntity).isNotNull();
    Assertions.assertThat(userEntity.getLastLoginAt()).isGreaterThan(0L);
  }

  @Test
  void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬수_있다() {
    // given
    // when
    userService.verifyEmail(2, "bbbbbbbbbb");

    // then
    UserEntity userEntity = userService.getById(2L);
    Assertions.assertThat(userEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_에러를_던진다() {
    // given
    // when
    // then
    Assertions.assertThatThrownBy(() -> {
      userService.verifyEmail(2, "bbbbbbbbba");
    }).isInstanceOf(CertificationCodeNotMatchedException.class);
  }


}