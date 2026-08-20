package com.adnibog.gamecenter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.adnibog.gamecenter.config.WebConfig;
import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.exception.ForbiddenException;
import com.adnibog.gamecenter.interceptor.AdminAuthInterceptor;
import com.adnibog.gamecenter.interceptor.ProjectInterceptor;
import com.adnibog.gamecenter.service.JwtService;
import com.adnibog.gamecenter.service.ProjectService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ProjectController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    WebConfig.class, AdminAuthInterceptor.class, ProjectInterceptor.class }))
class ProjectControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProjectService projectService;

  @MockBean
  private JwtService jwtService;

  @Test
  void createProject_Success() throws Exception {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId("proj_123");
    projectDto.setName("Test Project");

    when(projectService.createProject(any(), any(CreateProjectRequest.class))).thenReturn(projectDto);

    String json = "{\"name\":\"Test Project\"}";

    mockMvc.perform(post("/projects")
        .requestAttr("adminId", "admin_123")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value("proj_123"))
        .andExpect(jsonPath("$.data.name").value("Test Project"));
  }

  @Test
  void createProject_Forbidden_Returns403() throws Exception {
    when(projectService.createProject(any(), any(CreateProjectRequest.class)))
        .thenThrow(new ForbiddenException("Only SUPER_ADMIN can create projects"));

    String json = "{\"name\":\"Test Project\"}";

    mockMvc.perform(post("/projects")
        .requestAttr("adminId", "subadmin_123")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(json))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Only SUPER_ADMIN can create projects"));
  }

  @Test
  void listProjects_Success() throws Exception {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setId("proj_123");
    projectDto.setName("Test Project");

    when(projectService.listProjects("admin_123", 10, null, null))
        .thenReturn(new com.adnibog.gamecenter.dto.response.ProjectPageResponse(List.of(projectDto), null));

    mockMvc.perform(get("/projects")
        .requestAttr("adminId", "admin_123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[0].id").value("proj_123"))
        .andExpect(jsonPath("$.data.items[0].name").value("Test Project"));
  }

  @Test
  void deleteProject_Success() throws Exception {
    mockMvc.perform(delete("/projects/proj_123")
        .requestAttr("adminId", "admin_123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Project deleted successfully."));
  }
}
