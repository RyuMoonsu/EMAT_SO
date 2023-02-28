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
public class NodeInfoDto implements Serializable {
    private String nodeId;
    private String deviceId;
    private String deviceModel;
    private String serviceCode;
    private String nodeType;
    private String ctn;
    private String mac;
    private String deviceSerial;
    private String iccid;
    private String entityId;
    private String enrmKey;
    private String token;
    private String enrmKeyId;
    private String reachable;
    private String regTime;
}
