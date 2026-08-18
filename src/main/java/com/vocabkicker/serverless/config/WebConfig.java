package com.vocabkicker.serverless.config;

import com.vocabkicker.serverless.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigin, "http://localhost:3000")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "Cookie")
        .allowCredentials(true)
        .maxAge(3600);
  }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admins/**", "/questions/**");
    }
}
