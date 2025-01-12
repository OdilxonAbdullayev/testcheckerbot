package uz.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString(callSuper = true)
public class SertificatTestCheckerMilliyDto {
    private Float part_1;
    private Float part_2;
    private Float part_3;
    private Float part_4;
    private Float overallScore;
    private String fio;
}

