package movie_rating_backend.interceptor;

import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.Result;
import movie_rating_backend.utils.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        AuthRequired authRequired = method.getAnnotation(AuthRequired.class);
        if (authRequired == null) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401, \"message\":\"未登录或登录失效\"}");
            return false;
        }

        token = token.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        
        if (authRequired.admin()) {
            // 这里可以添加管理员权限检查逻辑
            // 例如：检查用户角色是否为管理员
        }

        // 将 userId 存入 ThreadLocal
        UserContextHolder.setUserId(userId);
        
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