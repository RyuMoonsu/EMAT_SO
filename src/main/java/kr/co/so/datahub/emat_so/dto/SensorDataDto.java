package kr.co.so.datahub.emat_so.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataDto implements Serializable {
    private String sensorId;
    private String dataValue;
    private String measureDt;
}
