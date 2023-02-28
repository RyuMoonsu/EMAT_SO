package kr.co.so.datahub.emat_so.mapper;

import kr.co.so.datahub.emat_so.dto.CommandLogDto;
import kr.co.so.datahub.emat_so.dto.SensorDataDto;
import kr.co.so.datahub.emat_so.dto.SensorTypeInfoDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmatSoMapper {
    int insertSensorData(SensorDataDto sensorDataDto);
    int updateRecentData(SensorDataDto sensorDataDto);
    int insertRecentData(SensorDataDto sensorDataDto);
    List<SensorTypeInfoDto> selectSensorTypeByNodeId(String nodeId);
    List<CommandLogDto> selectCommandLogByNodeId(String nodeId);

    int updateCommandLogByCommandId(String commandId);
}
