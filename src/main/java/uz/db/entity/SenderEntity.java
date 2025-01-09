package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uz.core.base.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SenderEntity extends BaseEntity {
    private String sendStatus;
    private String startTime;
    private Long sendCount;
    private Long sendLimitCount;
    private Long sendUser;
    private Long notSendUser;
    private Long messageId;
    private Long admin_id;
    private Long admin_messageId;
}
