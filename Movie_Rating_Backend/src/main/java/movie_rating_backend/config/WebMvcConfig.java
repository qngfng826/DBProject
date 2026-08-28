package movie_rating_backend.config;

import movie_rating_backend.interceptor.AuthInterceptor;
import movie_rating_backend.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/login", "/api/register",
                        "/api/report/**",
                        "/api/comment/movie/**", "/api/comment/user/**",
                        "/api/rating/movie/**",
                        "/api/movie/**",
                        "/api/actor/**",
                        "/api/director/**"
                );
        
        // 注册 AuthInterceptor
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/login", "/api/register",
                        "/api/report/**",
                        "/api/comment/movie/**", "/api/comment/user/**",
                        "/api/rating/movie/**",
                        "/api/movie/**",
                        "/api/actor/**",
                        "/api/director/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
