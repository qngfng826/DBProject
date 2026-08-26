package movie_rating_backend.controller;

import movie_rating_backend.entity.Rating;
import movie_rating_backend.entity.MovieRating;
import movie_rating_backend.utils.Result; // 假设你的统一返回类叫 Result，如果叫其他名请改一下
import movie_rating_backend.service.RatingService;
import movie_rating_backend.utils.JwtUtil;
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
    // 确保使用POST方法，并正确解析参数
    @PostMapping
    public Result<String> addRating(@RequestBody Rating rating, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = JwtUtil.getUserId(token);
            rating.setUserId(userId);

            // 设置评分时间
            rating.setRatingTime(LocalDateTime.now());

            ratingService.rateMovie(rating);
            return Result.success("评分成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteRating(@PathVariable("id") Integer ratingId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = JwtUtil.getUserId(token);
            // 验证评分是否属于当前用户（可选，推荐在Service层做）
            Rating rating = ratingService.getById(ratingId);
            if (rating == null) {
                return Result.error(404, "评分记录不存在");
            }
            if (!rating.getUserId().equals(userId)) {
                return Result.error(403, "无权删除此评分");
            }
            ratingService.removeRating(ratingId);
            return Result.success("评分已删除");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
    // 新增：通过电影ID删除当前用户的评分
    @DeleteMapping("/movie/{movieId}")
    public Result<String> deleteRatingByMovieId(@PathVariable("movieId") Integer movieId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = JwtUtil.getUserId(token);

            // 调用Service层方法，根据userId和movieId删除评分
            ratingService.removeByUserIdAndMovieId(userId, movieId);

            return Result.success("评分已取消");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 添加更新评分方法（如果不存在）
    @PostMapping("/update")
    public Result<String> updateRating(@RequestBody Rating rating, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = JwtUtil.getUserId(token);
            rating.setUserId(userId);

            ratingService.updateRating(rating);
            return Result.success("评分更新成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }


    // 获取当前用户对某电影的评分
    @GetMapping("/movie/{movieId}")
    public Result<Rating> getUserMovieRating(@PathVariable("movieId") Integer movieId, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = JwtUtil.getUserId(token);
            Rating rating = ratingService.getUserMovieRating(userId, movieId); // 需要在 Service 中实现
            return Result.success(rating);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
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
    public Result<List<MovieRating>> getUserRatings(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = JwtUtil.getUserId(token);
            // 假设 RatingService 有 getUserRatings 方法，没有需在 Service/Mapper 补全
            List<MovieRating> list = ratingService.getUserRatings(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}

