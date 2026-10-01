package movie_rating_backend.config;

import lombok.extern.slf4j.Slf4j;
import movie_rating_backend.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 容错处理：拦截异常不使程序中断；堆栈进日志便于排查，原始信息仍随响应返回
        log.error("未处理异常: {}", e.getMessage(), e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}
