package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.core.base.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserAnswer extends BaseEntity {
    private Long user_id;
    private Long subject_id;
    private String subject_name;
    private String security_key;
    private String subject_type;
    private int all_answer_count;
    private int correct_answer_count;
    private int incorrect_answer_count;
    private float ball;
    private float percentage; // foiz %
    private String incorrect_answers_list;
    private String allAnswersList;
}
