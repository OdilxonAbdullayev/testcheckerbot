package uz.db.entity;

import uz.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminEntity extends BaseEntity {
    private LocalDateTime created_date;
    private Long created_users_id;
}
