package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("director1707")
public class Director {
    @TableId(type = IdType.AUTO)
    private Integer directorId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String nationality;
}
