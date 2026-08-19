package com.adnibog.gamecenter.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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

import com.adnibog.gamecenter.config.WebConfig;
import com.adnibog.gamecenter.dto.internal.AuthResult;
import com.adnibog.gamecenter.exception.UnauthorizedException;
import com.adnibog.gamecenter.interceptor.AdminAuthInterceptor;
import com.adnibog.gamecenter.interceptor.ProjectInterceptor;
import com.adnibog.gamecenter.service.AuthService;
import com.adnibog.gamecenter.service.JwtService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    WebConfig.class, AdminAuthInterceptor.class, ProjectInterceptor.class }))
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthService authService;

  @MockBean
  private JwtService jwtService;

  @Test
  void login_Success() throws Exception {
    AuthResult authResult = new AuthResult("access_123", "refresh_123");
    when(authService.login(anyString(), anyString())).thenReturn(authResult);

    String loginJson = "{\"email\":\"admin@example.com\", \"password\":\"password123\"}";

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(loginJson))
        .andExpect(status().isOk())
        .andExpect(cookie().value("admin_token", "access_123"))
        .andExpect(cookie().value("refresh_token", "refresh_123"))
        .andExpect(jsonPath("$.data.message").value("Login successful"));
  }

  @Test
  void login_InvalidCredentials_Returns401() throws Exception {
    when(authService.login(anyString(), anyString())).thenThrow(new UnauthorizedException("Invalid credentials"));

    String loginJson = "{\"email\":\"admin@example.com\", \"password\":\"wrongpassword\"}";

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(loginJson))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("Invalid credentials"));
  }

  @Test
  void refresh_Success() throws Exception {
    AuthResult authResult = new AuthResult("new_access_123", "new_refresh_123");
    when(authService.refresh(anyString())).thenReturn(authResult);

    String refreshJson = "{\"refreshToken\":\"valid_refresh_token\"}";

    mockMvc.perform(post("/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(refreshJson))
        .andExpect(status().isOk())
        .andExpect(cookie().value("admin_token", "new_access_123"))
        .andExpect(cookie().value("refresh_token", "new_refresh_123"))
        .andExpect(jsonPath("$.data.message").value("Token refreshed"));
  }
}
