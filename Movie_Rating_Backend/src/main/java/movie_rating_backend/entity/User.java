package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user1707")
public class User {
    @TableId(value = "UserId", type = IdType.AUTO)
    private Integer userId;
    private String username;
    // WRITE_ONLY：密码哈希只进不出，任何接口的响应都不再携带（入参不受影响，管理员重置密码仍可用）
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String email;
//    @TableField(value = "RegisterTime")
    private Date registerTime;
}
