package movie_rating_backend.filter;

import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.UserContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 把 JWT 解析接入 Spring Security 过滤链：
 * 请求头携带有效 Bearer token 时，将认证信息放入 SecurityContext，
 * 使 SecurityConfig 中 anyRequest().authenticated() 规则能通过。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            try {
                token = token.substring(7);
                if (JwtUtil.validateToken(token)) {
                    Integer userId = JwtUtil.getUserId(token);

                    // 供 RatingController 等使用 UserContextHolder 的地方获取用户 id
                    UserContextHolder.setUserId(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // token 无效或过期：不设置认证信息，由 AuthenticationEntryPoint 返回 401
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 线程复用前清理 ThreadLocal（过滤器在拦截器 afterCompletion 之后才结束）
            UserContextHolder.clear();
        }
    }
}
