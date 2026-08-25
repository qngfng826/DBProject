package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user1707")
public class User {
    @TableField(value = "UserId")
    private Integer userId;
    private String username;
    private String password;
    private String email;
//    @TableField(value = "RegisterTime")
    private Date registerTime;
}
