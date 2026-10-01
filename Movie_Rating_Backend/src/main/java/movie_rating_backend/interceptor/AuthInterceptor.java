package movie_rating_backend.interceptor;

import lombok.extern.slf4j.Slf4j;
import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("=== AuthInterceptor 被调用 ===");
        log.info("请求路径: {} {}", method, requestURI);
        log.info("请求方法: {}", method);

        // 放行登录和注册接口，不需要认证
        if (requestURI.contains("/api/login") || requestURI.contains("/api/register")) {
            log.info("✓ 放行登录/注册接口: {}", requestURI);
            return true;
        }

        log.info("✗ 登录/注册接口外的请求，需要进行认证检查");

        if (!(handler instanceof HandlerMethod)) {
            log.info("✓ Handler 不是 HandlerMethod，放行");
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method handlerMethodObj = handlerMethod.getMethod();

        AuthRequired authRequired = handlerMethodObj.getAnnotation(AuthRequired.class);
        if (authRequired == null) {
            log.info("✓ 没有 @AuthRequired 注解，放行");
            return true;
        }

        log.info("✓ 发现 @AuthRequired 注解");

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("✗ 没有有效的 Authorization header");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401, \"message\":\"未登录或登录失效\"}");
            return false;
        }

        log.info("✓ 找到 Bearer token，长度: {}", token.length());

        token = token.substring(7);
        Integer userId = JwtUtil.getUserId(token);

        if (authRequired.admin()) {
            // 检查用户是否为管理员
            // 从 SecurityContext 中获取用户信息
                try {
                    org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated()) {
                        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = auth.getAuthorities();
                        boolean isAdmin = authorities.stream()
                                .anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));

                    if (!isAdmin) {
                        log.warn("✗ 用户 {} 不是管理员，需要管理员权限", userId);
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":403, \"message\":\"需要管理员权限\"}");
                        return false;
                    }
                    log.info("✓ 用户 {} 是管理员，有权限访问", userId);
                } else {
                    log.warn("✗ 用户未认证");
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403, \"message\":\"需要管理员权限\"}");
                    return false;
                }
            } catch (Exception e) {
                log.error("✗ 获取管理员权限失败", e);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403, \"message\":\"需要管理员权限\"}");
                return false;
            }
        }

        // 将 userId 存入 ThreadLocal
        UserContextHolder.setUserId(userId);

        log.info("✓ 认证通过，用户ID: {}", userId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
