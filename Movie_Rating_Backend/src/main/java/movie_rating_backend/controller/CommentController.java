package movie_rating_backend.controller;

import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.DTO.UserCommentDTO;
import movie_rating_backend.entity.Comment;
import movie_rating_backend.mapper.CommentMapper;
import movie_rating_backend.utils.Result;
import movie_rating_backend.service.CommentService;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.cache.CacheInvalidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheInvalidator cacheInvalidator;

    // 发表评论
    @PostMapping
    public Result<String> addComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");

            Integer userId = jwtUtil.getUserId(token);
            comment.setUserId(userId);

            commentService.save(comment);
            cacheInvalidator.onCommentWrite(comment.getMovieId());
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

            Integer userId = jwtUtil.getUserId(token);

            // 验证评论是否属于当前用户
            Comment existingComment = commentService.getById(id);
            if (existingComment == null || !existingComment.getUserId().equals(userId)) {
                return Result.error(403, "无权修改此评论");
            }

            comment.setCommentId(id);
            comment.setUserId(userId);
            comment.setCommentTime(LocalDateTime.now()); // 更新时间

            commentService.updateById(comment);
            cacheInvalidator.onCommentWrite(existingComment.getMovieId());
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

            Integer userId = jwtUtil.getUserId(token);

            // 验证评论是否属于当前用户
            Comment existingComment = commentService.getById(id);
            if (existingComment == null || !existingComment.getUserId().equals(userId)) {
                return Result.error(403, "无权删除此评论");
            }

            commentService.removeById(id);
            cacheInvalidator.onCommentWrite(existingComment.getMovieId());
            return Result.success("评论删除成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 获取某电影的所有评论
    @GetMapping("/movie/{movieId}")
    public Result<List<Comment>> getMovieComments(@PathVariable("movieId") Integer movieId) {
        try {
            List<Comment> list = commentMapper.getMovieCommentsWithUsername(movieId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 获取某用户的所有评论
    @GetMapping("/user")
    public Result<List<UserCommentDTO>> getUserComments(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error(401, "未登录");
            Integer userId = jwtUtil.getUserId(token);
            List<UserCommentDTO> list = commentService.getUserComments(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // === 后台评论管理接口 ===

    // 分页搜索评论（后台评论管理专用，关键字匹配评论内容/用户名/电影标题）
    @GetMapping("/search")
    @AuthRequired(admin = true)
    public Result<IPage<Comment>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Comment> pageObj = new Page<>(page, size);
            return Result.success(commentMapper.searchCommentsWithUsername(pageObj, keyword));
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 管理员编辑评论（不受"仅本人可改"限制，只更新内容）
    @PutMapping("/admin/{id}")
    @AuthRequired(admin = true)
    public Result<String> adminUpdate(@PathVariable Integer id, @RequestBody Comment comment) {
        try {
            Comment existing = commentService.getById(id);
            if (existing == null) return Result.error(404, "评论不存在");

            Comment toUpdate = new Comment();
            toUpdate.setCommentId(id);
            toUpdate.setContent(comment.getContent());
            boolean success = commentService.updateById(toUpdate);
            if (success) {
                cacheInvalidator.onCommentWrite(existing.getMovieId());
                return Result.success("评论更新成功");
            }
            return Result.error(500, "评论更新失败");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 管理员删除评论（不受"仅本人可删"限制）
    @DeleteMapping("/admin/{id}")
    @AuthRequired(admin = true)
    public Result<String> adminDelete(@PathVariable Integer id) {
        try {
            Comment existing = commentService.getById(id);
            if (existing == null) return Result.error(404, "评论不存在");

            boolean success = commentService.removeById(id);
            if (success) {
                cacheInvalidator.onCommentWrite(existing.getMovieId());
                return Result.success("评论删除成功");
            }
            return Result.error(500, "评论删除失败");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}

