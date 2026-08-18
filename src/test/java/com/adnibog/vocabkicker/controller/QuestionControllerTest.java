package com.adnibog.vocabkicker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.adnibog.vocabkicker.config.WebConfig;
import com.adnibog.vocabkicker.dto.request.CreateQuestionRequest;
import com.adnibog.vocabkicker.dto.request.UpdateQuestionRequest;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.interceptor.AdminAuthInterceptor;
import com.adnibog.vocabkicker.interceptor.ProjectInterceptor;
import com.adnibog.vocabkicker.service.JwtService;
import com.adnibog.vocabkicker.service.QuestionService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = QuestionController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    WebConfig.class, AdminAuthInterceptor.class, ProjectInterceptor.class }))
class QuestionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QuestionService questionService;

  @MockBean
  private JwtService jwtService;

  @Test
  void createQuestion_Success() throws Exception {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");

    QuestionDto questionDto = QuestionDto.builder().build();
    questionDto.setId("q_123");
    questionDto.setField1("Apple");
    questionDto.setProjects(projectDto);

    when(questionService.createQuestionFromRequest(eq("proj_123"), any(CreateQuestionRequest.class)))
        .thenReturn(questionDto);

    String json = "{\"dynamicFields\":{\"Word\":\"Apple\"}}";

    mockMvc.perform(post("/projects/proj_123/questions")
        .requestAttr("adminId", "admin_123")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value("q_123"))
        .andExpect(jsonPath("$.data.Word").value("Apple"));
  }

  @Test
  void updateQuestion_Success() throws Exception {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");

    QuestionDto questionDto = QuestionDto.builder().build();
    questionDto.setId("q_123");
    questionDto.setField1("Banana");
    questionDto.setProjects(projectDto);

    when(questionService.updateQuestion(eq("proj_123"), eq("q_123"), any(UpdateQuestionRequest.class)))
        .thenReturn(questionDto);

    String json = "{\"dynamicFields\":{\"Word\":\"Banana\"}}";

    mockMvc.perform(put("/projects/proj_123/questions/q_123")
        .requestAttr("adminId", "admin_123")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("q_123"))
        .andExpect(jsonPath("$.data.Word").value("Banana"));
  }

}
