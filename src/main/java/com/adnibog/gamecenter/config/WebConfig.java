package com.adnibog.gamecenter.config;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.adnibog.gamecenter.interceptor.AdminAuthInterceptor;
import com.adnibog.gamecenter.interceptor.ProjectInterceptor;

import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @NonNull
  private final AdminAuthInterceptor adminAuthInterceptor;
  @NonNull
  private final String[] allowedOrigins;

  @NonNull
  private final ProjectInterceptor projectInterceptor;

  public WebConfig(
      @NonNull AdminAuthInterceptor adminAuthInterceptor,
      @NonNull ProjectInterceptor projectInterceptor,
      @Value("${cors.allowed-origins}") @NonNull String[] allowedOrigins) {
    this.adminAuthInterceptor = adminAuthInterceptor;
    this.projectInterceptor = projectInterceptor;
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "Cookie")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/admins/**", "/projects/**", "/questions/**", "/uploads/**")
        .excludePathPatterns(
            "/auth/**",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
            "/projects/*/quiz");

    registry.addInterceptor(projectInterceptor)
        .addPathPatterns("/projects/{projectId}/**")
        .excludePathPatterns("/projects/{projectId}/quiz");
  }
}
