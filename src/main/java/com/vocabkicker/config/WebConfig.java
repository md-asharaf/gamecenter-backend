package com.vocabkicker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import com.vocabkicker.interceptor.AdminAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final AdminAuthInterceptor adminAuthInterceptor;
  private final String allowedOrigin;

  public WebConfig(AdminAuthInterceptor adminAuthInterceptor,
      @Value("${CORS_ALLOW_ORIGIN:https://vocabkicker.vercel.app}") String allowedOrigin) {
    this.adminAuthInterceptor = adminAuthInterceptor;
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
    registry.addInterceptor(java.util.Objects.requireNonNull(adminAuthInterceptor))
        .addPathPatterns("/admins/**", "/questions/**");
  }
}
