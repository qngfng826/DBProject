package movie_rating_backend.controller;

import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.entity.Rating;
import movie_rating_backend.entity.MovieRating;
import movie_rating_backend.utils.Result; // 假设你的统一返回类叫 Result，如果叫其他名请改一下
import movie_rating_backend.service.RatingService;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/rating")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    // 提交评分
    @PostMapping
    @AuthRequired
    public Result<String> addRating(@RequestBody Rating rating) {
        rating.setUserId(UserContextHolder.getUserId());
        ratingService.rateMovie(rating);
        return Result.success("评分成功");
    }

    @DeleteMapping("/{id}")
    @AuthRequired
    public Result<String> deleteRating(@PathVariable("id") Integer ratingId) {
        ratingService.removeRating(ratingId);
        return Result.success("评分已删除");
    }
    // 新增：通过电影ID删除当前用户的评分
    @DeleteMapping("/movie/{movieId}")
    @AuthRequired
    public Result<String> deleteRatingByMovieId(@PathVariable("movieId") Integer movieId) {
        Integer userId = UserContextHolder.getUserId();
        ratingService.removeByUserIdAndMovieId(userId, movieId);
        return Result.success("评分已取消");
    }

    // 添加更新评分方法（如果不存在）
    @PostMapping("/update")
    @AuthRequired
    public Result<String> updateRating(@RequestBody Rating rating) {
        Integer userId = UserContextHolder.getUserId();
        rating.setUserId(userId);
        ratingService.updateRating(rating);
        return Result.success("评分更新成功");
    }


    // 获取当前用户对某电影的评分
    @GetMapping("/movie/{movieId}")
    @AuthRequired
    public Result<Rating> getUserMovieRating(@PathVariable("movieId") Integer movieId) {
        Integer userId = UserContextHolder.getUserId();
        Rating rating = ratingService.getUserMovieRating(userId, movieId);
        return Result.success(rating);
    }

    // 获取某电影的平均评分
    @GetMapping("/movie/{movieId}/average")
    public Result<Double> getAverageRating(@PathVariable("movieId") Integer movieId) {
        try {
            Double averageRating = ratingService.getAverageRating(movieId);
            return Result.success(averageRating);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
    // 获取当前用户的所有评分
    @GetMapping("/user")
    @AuthRequired
    public Result<List<MovieRating>> getUserRatings() {
        Integer userId = UserContextHolder.getUserId();
        List<MovieRating> list = ratingService.getUserRatings(userId);
        return Result.success(list);
    }
}

