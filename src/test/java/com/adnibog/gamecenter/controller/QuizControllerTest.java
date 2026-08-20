package com.adnibog.gamecenter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.adnibog.gamecenter.config.WebConfig;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.dto.response.QuizQuestion;
import com.adnibog.gamecenter.interceptor.AdminAuthInterceptor;
import com.adnibog.gamecenter.interceptor.ProjectInterceptor;
import com.adnibog.gamecenter.service.JwtService;
import com.adnibog.gamecenter.service.QuizService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = QuizController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    WebConfig.class, AdminAuthInterceptor.class, ProjectInterceptor.class }))
class QuizControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QuizService quizService;

  @MockBean
  private JwtService jwtService;

  @Test
  void generateQuiz_Success() throws Exception {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");

    QuizQuestion quizQuestion = QuizQuestion.builder().build();
    quizQuestion.setField1("Apple");
    quizQuestion.setProjects(projectDto);

    when(quizService.generateQuiz("proj_123")).thenReturn(List.of(quizQuestion));

    mockMvc.perform(get("/projects/proj_123/quiz")
        .requestAttr("adminId", "admin_123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].Word").value("Apple"));
  }
}
