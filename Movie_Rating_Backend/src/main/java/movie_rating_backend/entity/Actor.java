package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("actor1707")
public class Actor {
    @TableId(type = IdType.AUTO)
    private Integer actorId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String nationality;
    private String photoUrl;

    @TableField(exist = false)
    private String roleName;
}
