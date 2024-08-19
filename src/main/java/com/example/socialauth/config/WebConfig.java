package com.example.socialauth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 로그로 세션 ID를 남겨서 세션이 유지되는지 확인
                HttpSession session = request.getSession(false);
                if (session == null) {
                    log.warn("Session does not exist.");
                } else {
                    log.info("Session ID: {}", session.getId());
                }
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
                if (modelAndView != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
                        String userRole = (String) session.getAttribute("userRole");

                        // 로그 추가
                        log.info("postHandle - Session ID: {}, isAuthenticated: {}, userRole: {}", session.getId(), isAuthenticated, userRole);

                        if (isAuthenticated != null) {
                            modelAndView.addObject("isAuthenticated", isAuthenticated);
                        }
                        if (userRole != null) {
                            modelAndView.addObject("userRole", userRole);
                        }
                    }
                }
            }
        });
    }
}
