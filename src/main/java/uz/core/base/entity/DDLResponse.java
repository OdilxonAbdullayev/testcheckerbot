package uz.core.base.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DDLResponse<ENTITY> {
    private Boolean status;
    private Integer code;
    private ENTITY data;
    private String responseMessage;
    private String logMessage;
}
