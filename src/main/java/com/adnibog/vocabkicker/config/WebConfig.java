package com.adnibog.vocabkicker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.adnibog.vocabkicker.interceptor.AdminAuthInterceptor;
import com.adnibog.vocabkicker.interceptor.ProjectInterceptor;

import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @NonNull
  private final AdminAuthInterceptor adminAuthInterceptor;
  private final String allowedOrigin;

  @NonNull
  private final ProjectInterceptor projectInterceptor;

  public WebConfig(
      @NonNull AdminAuthInterceptor adminAuthInterceptor,
      @NonNull ProjectInterceptor projectInterceptor,
      @Value("${cors.allowed-origin}") String allowedOrigin) {
    this.adminAuthInterceptor = adminAuthInterceptor;
    this.projectInterceptor = projectInterceptor;
    this.allowedOrigin = allowedOrigin;
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigin, "http://localhost:3000")
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
