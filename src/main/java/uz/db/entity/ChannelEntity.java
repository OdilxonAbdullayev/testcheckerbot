package uz.db.entity;

import uz.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChannelEntity extends BaseEntity {
    private Long created_users_id;
    private LocalDateTime created_date;
}
