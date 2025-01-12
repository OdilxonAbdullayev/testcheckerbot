package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString(callSuper = true)
public class SertificatAttestatsiyaEntity {
    private String fio;
    private String sort;
    private Float overallScore;
    private Float for70Score;
}
