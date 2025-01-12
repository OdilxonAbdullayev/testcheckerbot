package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.core.base.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Deprecated
public class AllStatisticEntity extends BaseEntity {
    private Long all_user_count;
    private Long active_user_count;
    private Long deleted_user_count;
}
