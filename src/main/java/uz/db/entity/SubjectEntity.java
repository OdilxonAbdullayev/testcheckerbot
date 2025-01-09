package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.core.base.entity.BaseEntity;
import uz.db.enums.QuizType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubjectEntity extends BaseEntity {
    private String name;
    private String security_key;
    private QuizType quiz_type;
    private LocalDateTime created_date;
    private LocalDateTime update_date;
    private Long created_user_id;
}
