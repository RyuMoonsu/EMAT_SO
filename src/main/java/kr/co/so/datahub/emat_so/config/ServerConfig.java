package kr.co.so.datahub.emat_so.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@Configuration
@PropertySource("classpath:server.properties")
public class ServerConfig {
    @Value("${SERVER_HOST}")
    private String serverHost;

    @Value("${SERVER_PORT}")
    private String serverPort;

    @Value("${PROJECT_ID}")
    private String projectId;

    @Value("${LOGS_ABSOLUTE_PATH}")
    private String logsAbsolutePath;

    @Value("${PROTOCOL_JSON}")
    private String protocolJson;

    @Value("${MAX_PACKET_SIZE}")
    private int MAX_PACKET_SIZE;

    @Value("${TIMESTAMP_PART}")
    private String TIMESTAMP_PART;
}
