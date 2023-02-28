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
public class DeviceDto implements Serializable {
    private String deviceId;
    private String facilityId;
    private String deviceName;
    private String deviceModel;
    private String deviceType;
    private String installDate;
    private String periodSec;
    private String periodCnt;
    private String manufacturer;
    private String serialNum;
    private String description;
    private String deviceStatus;
    private String serviceId;
}
