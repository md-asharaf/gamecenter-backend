package com.adnibog.gamecenter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.adnibog.gamecenter.dto.response.UploadUrlResponse;
import com.adnibog.gamecenter.interceptor.AdminAuthInterceptor;
import com.adnibog.gamecenter.interceptor.ProjectInterceptor;
import com.adnibog.gamecenter.service.UploadService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UploadController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    WebConfig.class, AdminAuthInterceptor.class, ProjectInterceptor.class }))
class UploadControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UploadService uploadService;

  @Test
  void generateUploadUrl_Success() throws Exception {
    UploadUrlResponse response = new UploadUrlResponse("https://s3.amazonaws.com/test", "proj_123/file.csv");

    when(uploadService.generateUploadUrl("proj_123", "folder1", "csv")).thenReturn(response);

    mockMvc.perform(post("/projects/proj_123/folders/folder1/uploads/presigned-url")
        .param("ext", "csv")
        .requestAttr("adminId", "admin_123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.url").value("https://s3.amazonaws.com/test"))
        .andExpect(jsonPath("$.data.key").value("proj_123/file.csv"));
  }
}
