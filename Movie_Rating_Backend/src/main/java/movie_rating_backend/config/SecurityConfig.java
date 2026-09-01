package movie_rating_backend.config;

import movie_rating_backend.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .antMatchers("/api/movie/**").permitAll()
                .antMatchers("/api/actor/**").permitAll()
                .antMatchers("/api/director/**").permitAll()
                .antMatchers("/api/comment/movie/**", "/api/comment/user/**").permitAll()
                .antMatchers("/api/rating/movie/**", "/api/rating/movie/{id:\\d+}/average", "/api/rating/user").permitAll()
                .antMatchers("/api/report/**").permitAll()
                .anyRequest().authenticated()
            )
            // 把 JWT 解析接入安全过滤链，认证信息需在授权检查之前设置好
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
