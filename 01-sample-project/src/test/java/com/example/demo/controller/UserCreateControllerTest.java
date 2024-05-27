package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.user.domain.UserCreate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SqlGroup({
    @Sql(
        value = "/sql/delete-all-data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class UserCreateControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private JavaMailSender mailSender;

  @Test
  void 사용자_정보를_생성할_수_있다() throws Exception {
    // given
    UserCreate userCreate = UserCreate.builder()
        .email("go13232@test.com")
        .nickname("go13232")
        .address("Incheon")
        .build();

    // when
    BDDMockito.doNothing().when(mailSender).send(any(SimpleMailMessage.class));

    // then
    mockMvc.perform(post("/api/users")
            .content(objectMapper.writeValueAsString(userCreate))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("go13232@test.com"))
        .andExpect(jsonPath("$.nickname").value("go13232"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }
}