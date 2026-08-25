package movie_rating_backend.controller;

import movie_rating_backend.entity.Comment;
import movie_rating_backend.entity.MovieComment;
import movie_rating_backend.utils.Result;
import movie_rating_backend.service.CommentService;
import movie_rating_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 发表评论
    @PostMapping
    public Result<String> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = JwtUtil.getUserId(token);
            comment.setUserId(userId);

            commentService.save(comment);
            return Result.success("评论成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> updateComment(
            @PathVariable Integer id,
            @RequestBody Comment comment,
            HttpServletRequest request) {

        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = JwtUtil.getUserId(token);

            // 验证评论是否属于当前用户
            Comment existingComment = commentService.getById(id);
            if (existingComment == null || !existingComment.getUserId().equals(userId)) {
                return Result.error(403, "无权修改此评论");
            }

            comment.setCommentId(id);
            comment.setUserId(userId);
            comment.setCommentTime(LocalDateTime.now()); // 更新时间

            commentService.updateById(comment);
            return Result.success("评论更新成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = JwtUtil.getUserId(token);

            // 验证评论是否属于当前用户
            Comment existingComment = commentService.getById(id);
            if (existingComment == null || !existingComment.getUserId().equals(userId)) {
                return Result.error(403, "无权删除此评论");
            }

            commentService.removeById(id);
            return Result.success("评论删除成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 获取某电影的所有评论
    @GetMapping("/movie/{movieId}")
    public Result<List<Comment>> getMovieComments(@PathVariable("movieId") Integer movieId) {
        try {
            // 假设你的 CommentService 中有 getCommentsByMovieId 方法
            // 比如： List<Comment> list = commentService.getCommentsByMovieId(movieId);
            List<Comment> list = commentService.getCommentsByMovieId(movieId); // 需要在 Service 中实现
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 获取某用户的所有评论
    @GetMapping("/user")
    public Result<List<MovieComment>> getUserComments(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = JwtUtil.getUserId(token);
            List<MovieComment> list = commentService.getUserComments(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}

