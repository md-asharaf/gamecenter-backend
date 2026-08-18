package com.adnibog.gamecenter.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  private final String[] allowedOrigins;

  @NonNull
  private final ProjectInterceptor projectInterceptor;

  public WebConfig(
      @NonNull AdminAuthInterceptor adminAuthInterceptor,
      @NonNull ProjectInterceptor projectInterceptor,
      @Value("${cors.allowed-origins}") String[] allowedOrigins) {
    this.adminAuthInterceptor = adminAuthInterceptor;
    this.projectInterceptor = projectInterceptor;
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    List<String> origins = new ArrayList<>(Arrays.asList(allowedOrigins));
    if (!origins.contains("http://localhost:3000")) {
      origins.add("http://localhost:3000");
    }

    registry.addMapping("/**")
        .allowedOrigins(java.util.Objects.requireNonNull(origins.toArray(new String[0])))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "Cookie")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/admins/**", "/projects/**", "/questions/**", "/uploads/**")
        .excludePathPatterns("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**");

    registry.addInterceptor(projectInterceptor)
        .addPathPatterns("/projects/{projectId}/**");
  }
}
