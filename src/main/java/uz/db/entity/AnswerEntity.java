package uz.db.entity;

import uz.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.db.enums.QuizType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnswerEntity extends BaseEntity {
    private String answer;
    private Float score;
    private Long subject_id;
    private LocalDateTime created_date;
    private Long creator_user_id;
}
