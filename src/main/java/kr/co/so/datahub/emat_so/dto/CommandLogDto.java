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
public class CommandLogDto implements Serializable {
    private String commandId;
    private String nodeId;
    private String deviceId;
    private String command;
    private String sendData;
    private String recordDt;
    private String sendDt;
    private String response;
    private String commandStatus;
    private String description;
}
