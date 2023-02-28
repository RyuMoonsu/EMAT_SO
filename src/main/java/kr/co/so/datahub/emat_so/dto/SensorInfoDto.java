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
public class SensorInfoDto implements Serializable {
    private String sensorId;
    private String deviceId;
    private String typeId;
    private String sensorName;
    private String manufacturer;
    private String serialNum;
    private String visible;
    private String installDate;
    private String description;
    private String sensorStatus;
    private String offsetValue;
    private String calA;
    private String calB;
    private String calC;
}
