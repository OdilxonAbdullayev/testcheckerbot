package uz.db.entity;

import uz.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserEntity extends BaseEntity {
    private String step;
    private Integer status_id;
    private String username;
    private String current_security_key;
    private LocalDateTime created_date;
    private LocalDateTime updated_date;
}
