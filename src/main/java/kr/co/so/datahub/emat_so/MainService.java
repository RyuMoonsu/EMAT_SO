package kr.co.so.datahub.emat_so;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.co.so.datahub.emat_so.config.ServerConfig;
import kr.co.so.datahub.emat_so.dto.CommandLogDto;
import kr.co.so.datahub.emat_so.dto.SensorDataDto;
import kr.co.so.datahub.emat_so.dto.SensorTypeInfoDto;
import kr.co.so.datahub.emat_so.mapper.EmatSoMapper;
import kr.co.so.datahub.emat_so.util.CrcUtil;
import kr.co.so.datahub.emat_so.util.HexUtil;
import kr.co.so.datahub.emat_so.util.Parser;
import lombok.SneakyThrows;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@MapperScan(basePackages="kr.co.so.datahub.emat_so.mapper")
@PropertySource("classpath:server.properties")
public class MainService {
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);

    ServerConfig serverConfig;

    @Value("${SERVER_HOST}")
    private String serverHost;

    @Value("${SERVER_PORT}")
    private int serverPort;

    @Value("${PROJECT_ID}")
    private String projectId;

    @Value("${DACO_ID}")
    private String dacoId;

    @Value("${PROTOCOL_JSON}")
    private String protocolJson;

    @Value("${CRC_CHECK:true}")
    private boolean CRC_CHECK;

    @Value("${MAX_PACKET_SIZE}")
    private int MAX_PACKET_SIZE;

    @Value("${TIMESTAMP_PART}")
    private String TIMESTAMP_PART;
    private int TIMESTAMP_START;
    private int TIMESTAMP_END;

    @Autowired
    private EmatSoMapper ematSoMapper;

    private final static int TWO_DIGIT = 2;
    private final static int ackLength = 22; // STX 빼고 전체 길이

    public Parser parser;
    public HexUtil hexUtil;
    public CrcUtil crcUtil;
    public JsonObject protocolObject;
    private Gson gson = new Gson();

    private long connnectTimeout = 40L;

    public void ready() {
        logger.error(String.format("----------------------- [%s] READY [%s]------------------------", projectId, dacoId));

        protocolObject = new JsonParser().parse(protocolJson).getAsJsonObject();
        hexUtil = new HexUtil();
        crcUtil = new CrcUtil();
        parser = new Parser(protocolObject, crcUtil, hexUtil);
        String[] parts = StringUtils.split(TIMESTAMP_PART,":");
        TIMESTAMP_START = Integer.valueOf(parts[0]) - 1; // 0 base 때문에 -1 한다
        TIMESTAMP_END = Integer.valueOf(parts[1]) - 1; // 0 base 때문에 -1 한다
    }

    public void startThread() throws IOException {
        InetSocketAddress sAdder = new InetSocketAddress(serverPort);
        AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory());
        AsynchronousServerSocketChannel serverSock = AsynchronousServerSocketChannel.open(channelGroup);
        serverSock.bind(sAdder);

        logger.error(String.format("----------------------- [%s] START [%s]------------------------", projectId, dacoId));
        logger.error("SERVER ADDRESS: "+sAdder+", "+serverSock);

        //start to accept the connection from client
        serverSock.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            //@SneakyThrows
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                socketRead(result);
                serverSock.accept(null, this);
            }

            //@SneakyThrows
            @Override
            public void failed(Throwable exc, Void attachment) {
                //logger.error("Fail to accept a connection: {}", exc);
            }
        });
    }

    private void socketRead(AsynchronousSocketChannel asynchronousSocketChannel) {
        final ByteBuffer buf = ByteBuffer.allocate(MAX_PACKET_SIZE);
        asynchronousSocketChannel.read( buf, connnectTimeout, TimeUnit.SECONDS, null , new CompletionHandler<Integer, Void>() {
            @SneakyThrows
            @Override
            public void completed(Integer result, Void attachment) {
                if(result < 0) {
                    logger.error("Device was disconnected. "+asynchronousSocketChannel.getRemoteAddress());
                    asynchronousSocketChannel.close();
                } else if(result > 0) {
                    buf.flip();
                    String hexString = hexUtil.byteArrayToHex(buf.array());
                    hexString = StringUtils.substring(hexString, 0, buf.limit()*2);
                    logger.info("[I][{}][{}]{}", dacoId, asynchronousSocketChannel, hexString);

                    if(StringUtils.isNotEmpty(hexString)) {
                        String jsonParsed = null;
                        try {
                            jsonParsed = parser.parse(hexString);
                            JsonObject parsedJo = gson.fromJson(jsonParsed, JsonObject.class);

                            if(null != parsedJo.get("_FAILURE_") || parsedJo.isJsonNull()) {
                                String reason = parsedJo.get("_FAILURE_").getAsString();
                                //SensorDataDto sensorDataDto = new SensorDataDto(0, "", 0, "", hexString, reason);
                                //dacoMapper.insertAbnormalData(sensorDataDto);
                                logger.info("[E][{}][{}]{}", dacoId, asynchronousSocketChannel, "_FAILURE_|"+reason);
                            } else {
                                logger.info("[P][{}][{}]{}", dacoId, asynchronousSocketChannel, jsonParsed);
                                String nodeId = parsedJo.get(Constants.NODE_ID).getAsString();
                                String clazz = parsedJo.get(Constants.CLASS).getAsString();
                                String func = parsedJo.get(Constants.FUNC).getAsString();
                                String param1 = parsedJo.get(Constants.PARAM1).getAsString();
                                String param2 = parsedJo.get(Constants.PARAM2).getAsString();

                                logger.info("RESOLVE:"+nodeId+","+clazz+","+func+","+param1+","+param2);
                                switch(clazz) {
                                    case "S": // SET
                                    case "G": // GET
                                    case "D": // DATA
                                        switch(func) {
                                            case "D": // DEVICE
                                                switch(param1) {
                                                    case "S": // SENSOR
                                                        switch(param2) {
                                                            case "D": // DATA
                                                                boolean isSuccess = saveDatabase(parsedJo, nodeId, hexString, asynchronousSocketChannel.getRemoteAddress().toString());
                                                                String ackTimestamp = StringUtils.substring(hexString, TIMESTAMP_START*2, TIMESTAMP_END*2);
                                                                ByteBuffer ackPacket = null;
                                                                if(isSuccess) {
                                                                    List<CommandLogDto> commandLogDtoList = getCommandLog(nodeId);
                                                                    if(commandLogDtoList.size() > 0) { // 장비로 보낼 제어명령가 있는 경우
                                                                        ackTimestamp += "01"; // 장비에 40초간 연결 유지하도록 설정. 40초 동안 명령어를 전부 보내야 함
                                                                    } else {
                                                                        ackTimestamp += "00"; // 장비에 바로 연결 종료하도록 설정
                                                                    }
                                                                    ackPacket = makePacket(nodeId, "DACK", ackTimestamp);
                                                                    socketWrite(asynchronousSocketChannel, ackPacket);

                                                                    if(commandLogDtoList.size() > 0) {
                                                                        for(CommandLogDto commandLogDto : commandLogDtoList) {
                                                                            String commandId = commandLogDto.getCommandId();
                                                                            String command = commandLogDto.getCommand();
                                                                            String sendData = commandLogDto.getSendData();

                                                                            ByteBuffer commPacket = makePacket(nodeId, command, sendData);
                                                                            socketWrite(asynchronousSocketChannel, commPacket);
                                                                            ematSoMapper.updateCommandLogByCommandId(commandId);
                                                                        }
                                                                    }
                                                                } else { // 장비 에러 안나게 계속 ACK 만 보냄
                                                                    ackPacket = makePacket(nodeId, "DACK", ackTimestamp+"00");
                                                                    socketWrite(asynchronousSocketChannel, ackPacket);
                                                                }
                                                                logger.info("SAVE:"+isSuccess+", ACK:"+ hexUtil.byteArrayToHex(ackPacket.array()));
                                                                break;
                                                        }
                                                        break;
                                                }
                                                break;
                                        }
                                        break;
                                }
                            }
                        } catch (DecoderException e) {
                            //throw new RuntimeException(e);
                            logger.error("Parse Error: ", e);
                        }
                    }
                } else {
                    //
                }
                buf.clear();
                socketRead(asynchronousSocketChannel);
            }

            @SneakyThrows
            @Override
            public void failed(Throwable exc, Void attachment) {
                //logger.error("Fail to read a connection: {}", exc);
                asynchronousSocketChannel.close();
            }
        });
    }

    private ByteBuffer makePacket(String ctn, String command, String hexString) {
        int stxBytes = 1;
        int lengthBytes = 2;
        int ctnBytes = 6;
        int headBytes = 4;
        int dataBytes = (hexString.length()/2);
        int crcBytes = 2;
        int etxBytes = 1;
        int packetLength = ctnBytes+headBytes+dataBytes+crcBytes+etxBytes;

        ByteBuffer ackPacket = ByteBuffer.allocate(stxBytes+lengthBytes+packetLength);
        ackPacket.put((byte)2);
        ackPacket.put(hexUtil.shortToByteArray((short)(packetLength), true));
        ackPacket.put(hexUtil.hexToByteArray(ctn));
        char[] commandArray = command.toCharArray();
        for(char c : commandArray) {
            ackPacket.put((byte)c);
        }
        ackPacket.put(hexUtil.hexStringToByteArray(hexString));
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<ackPacket.position(); i++) {
            sb.append(String.format("%02X", ackPacket.get(i)));
        }
        String crcHex = sb.toString();
        ackPacket.put(hexUtil.hexToByteArray(hexUtil.reverseHex(crcUtil.makeCRC16(crcHex))));
        ackPacket.put((byte)3);
        ackPacket.flip();

        return ackPacket;
    }

    private boolean saveDatabase(JsonObject jo, String nodeId, String hexString, String fromClient) {
        boolean isSuccess = false;
        List<SensorTypeInfoDto> sensorTypeInfoList = ematSoMapper.selectSensorTypeByNodeId(nodeId);
        if(sensorTypeInfoList.size() < 1) {
            logger.error("Node(CTN) {} does not have a sensor nor register device.", nodeId);
        }
        for(SensorTypeInfoDto sensorTypeInfo : sensorTypeInfoList) {
            String typeCode = sensorTypeInfo.getTypeCode();
            if(null == jo.get(typeCode)) {
                logger.error("TYPE CODE: "+typeCode+", "+jo.toString());
                continue;
            }
            String sensorId = sensorTypeInfo.getSensorId();
            String dataValue = jo.get(typeCode).getAsString();
            String measureDt = jo.get(Constants.TIMESTAMP).getAsString();

            SensorDataDto sensorDataDto = new SensorDataDto(sensorId, dataValue, measureDt);
            int ret = ematSoMapper.updateRecentData(sensorDataDto);
            isSuccess = ret < 1 ? false : true;
            if(!isSuccess) {
                logger.error("INFO New Device Recent insert: {},{},{} ", sensorId, dataValue, measureDt);
                ret = ematSoMapper.insertRecentData(sensorDataDto);
                isSuccess = ret < 1 ? false : true;
                if(!isSuccess) {
                    logger.error("Failure Recent insert: {},{},{} ", sensorId, dataValue, measureDt);
                } else {
                    logger.error("Failure: Recent insert: {},{},{} ", sensorId, dataValue, measureDt);
                }
            }
            ret = ematSoMapper.insertSensorData(sensorDataDto);
            isSuccess = ret < 1 ? false : true;
            if(!isSuccess) {
                logger.error("Failure Sensor DATA insert: {},{},{} ", sensorId, dataValue, measureDt);
            }
        }
        return isSuccess;
    }

    private List<CommandLogDto> getCommandLog(String nodeId) {
        return ematSoMapper.selectCommandLogByNodeId(nodeId);
    }
    private void socketWrite(AsynchronousSocketChannel sockChannel, final ByteBuffer buf) {
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                //finish to write message to client, nothing to do
                //System.out.println("send result: "+result.intValue()+" bytes");
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                //fail to write message to client
                //logger.error("Fail to write message to client. {}", channel.getRemoteAddress());
            }
        });
    }
}
