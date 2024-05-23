package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.UserStatus;
import com.example.demo.model.dto.UserUpdateDto;
import com.example.demo.repository.UserEntity;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SqlGroup({
    @Sql(
        value = "/sql/user-controller-test-data.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(
        value = "/sql/delete-all-data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void 사용자는_특정_유저의_정보를_개인정보를_소거하고_전달_받을_수_있다() throws Exception {
    // given
    // when
    // then
    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("go1323@test.com"))
        .andExpect(jsonPath("$.nickname").value("go1323"))
        .andExpect(jsonPath("$.address").doesNotExist())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void 사용자는_존해하지_않는_유저의_아이디로_api_호출할_경우_404_응답을_받는다() throws Exception {
    // given
    // when
    // then
    mockMvc.perform(get("/api/users/99999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Users에서 ID 99999를 찾을 수 없습니다."))
    ;
  }

  @Test
  void 사용자는_인증코드로_계정을_활성화_시킬_수_있다() throws Exception {
    // given
    // when
    // then
    mockMvc.perform(get("/api/users/2/verify")
            .queryParam("certificationCode", "bbbbbbbbbb"))
        .andExpect(status().isFound());

    UserEntity userEntity = userRepository.findById(2L).get();
    Assertions.assertThat(userEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void 사용자는_인증코드가_일치하지_않을_경우_권한_없음_에러를_준다() throws Exception {
    // given
    // when
    // then
    mockMvc.perform(get("/api/users/2/verify")
            .queryParam("certificationCode", "aaaaaaaaaa"))
        .andExpect(status().isForbidden());
  }

  @Test
  void 사용자는_내_정보를_불러올_때_개인정보인_주수도_갖고_올_수_있다() throws Exception {
    // given
    // when
    // then
    mockMvc.perform(get("/api/users/me")
            .header("EMAIL", "go1323@test.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("go1323@test.com"))
        .andExpect(jsonPath("$.address").value("Seoul"))
        .andExpect(jsonPath("$.nickname").value("go1323"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void 사용자는_내_정보를_수정할_수_있다() throws Exception {
    // given
    UserUpdateDto userUpdateDto = UserUpdateDto.builder()
        .nickname("go1323-new")
        .address("busan")
            .build();

    // when
    // then
    mockMvc.perform(put("/api/users/me")
            .header("EMAIL", "go1323@test.com")
            .content(objectMapper.writeValueAsString(userUpdateDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("go1323@test.com"))
        .andExpect(jsonPath("$.address").value("busan"))
        .andExpect(jsonPath("$.nickname").value("go1323-new"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }
}