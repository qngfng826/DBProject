package movie_rating_backend.config;

import movie_rating_backend.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .cors()  // 启用 CORS 支持，使用 WebMvcConfig 中的配置
            .and()
            // 未认证（无 token / token 失效）时返回 401，而不是默认的 403
            .exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401, \"message\":\"未登录或登录失效\"}");
            })
            .and()
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/api/login", "/api/register").permitAll()
                // 2026-11-21 收紧：permitAll 一律限定 HttpMethod.GET——
                // 此前 path-only 通配把 POST/PUT/DELETE 也匿名放行，写操作仅剩拦截器单层防护；
                // 现在全部写操作与用户私有读接口落入 authenticated()，与拦截器 @AuthRequired 构成双层防护
                .antMatchers(HttpMethod.GET, "/api/movie/**", "/api/actor/**", "/api/director/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/comment/movie/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/report/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/rating/movie/{id:\\d+}/average").permitAll()
                .anyRequest().authenticated()
            )
            // 把 JWT 解析接入安全过滤链，认证信息需在授权检查之前设置好
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
