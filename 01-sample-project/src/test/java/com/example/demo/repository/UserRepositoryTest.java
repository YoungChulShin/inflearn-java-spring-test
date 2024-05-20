package com.example.demo.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.demo.model.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest(showSql = true)
@TestPropertySource("classpath:test-application.properties")
@Sql("/sql/user-repository-test-data.sql")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

//  @Test
//  void UserRepository_가_제대로_연결되었다() {
//    // given
//    UserEntity userEntity = new UserEntity();
//    userEntity.setEmail("go1323@test.com");
//    userEntity.setAddress("Seoul");
//    userEntity.setNickname("go1323");
//    userEntity.setStatus(UserStatus.ACTIVE);
//    userEntity.setCertificationCode("aaaaaa");
//
//    // when
//    UserEntity result = userRepository.save(userEntity);
//
//    // then
//    assertThat(result.getId()).isNotNull();
//  }

  @Test
  void findByIdAndStatus_로_유저_데이터를_찾아올_수_있다() {
    // when
    Optional<UserEntity> result = userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE);

    // then
    assertThat(result.isPresent()).isTrue();
  }

  @Test
  void findByIdAndStatus_는_데이터가_없으면_Optional_Empty_를_내려준다() {
    // when
    Optional<UserEntity> result = userRepository.findByIdAndStatus(1L, UserStatus.PENDING);

    // then
    assertThat(result).isEmpty();
  }
}