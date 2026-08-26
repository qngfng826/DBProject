package movie_rating_backend.config;

import movie_rating_backend.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 容错处理：拦截异常不使程序中断
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}
