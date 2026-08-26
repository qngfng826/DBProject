package movie_rating_backend;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("movie_rating_backend.mapper")
public class MovieRatingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieRatingSystemApplication.class, args);
    }
}
