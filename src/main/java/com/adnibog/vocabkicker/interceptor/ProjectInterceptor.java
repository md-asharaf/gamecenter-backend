package com.adnibog.vocabkicker.interceptor;

import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.entity.Role;
import com.adnibog.vocabkicker.exception.UnauthorizedException;
import com.adnibog.vocabkicker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

@Component
public class ProjectInterceptor implements HandlerInterceptor {

  private final UserRepository userRepository;

  public ProjectInterceptor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler) throws Exception {

    if (request.getMethod().equals("OPTIONS")) {
      return true;
    }

    String adminId = (String) request.getAttribute("adminId");
    if (adminId == null) {
      throw new UnauthorizedException("Admin authentication required");
    }

    Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(attribute instanceof Map<?, ?>)) {
      return true;
    }

    Map<?, ?> pathVariables = (Map<?, ?>) attribute;
    Object projectIdObj = pathVariables.get("projectId");
    if (!(projectIdObj instanceof String)) {
      return true;
    }

    String projectId = (String) projectIdObj;

    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new UnauthorizedException("Admin not found"));

    if (admin.getRole() == Role.SUPER_ADMIN) {
      return true;
    }

    Set<String> allowedProjects = admin.getProjectIds();
    if (allowedProjects == null || !allowedProjects.contains(projectId)) {
      throw new UnauthorizedException("Admin does not have access to this project");
    }

    return true;
  }
}
