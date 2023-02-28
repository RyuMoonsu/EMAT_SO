package kr.co.so.datahub.emat_so.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorTypeInfoDto implements Serializable {
    private String nodeId;
    private String ctn;
    private String sensorId;
    private String typeId;
    private String typeCode;
}
