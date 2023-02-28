package kr.co.so.datahub.emat_so;

/**
 * 상수 정의 클래스
 */
public class Constants {
    public static final String BASE_DATETIME = "2000-01-01T00:00:00";
    public static final String STX = "STX";
    public static final String ETX = "ETX";
    public static final String LENGTH = "LENGTH";
    public static final String GROUP_ID = "GROUP_ID";
    public static final String NODE_ID = "NODE_ID";
    public static final String CLASS = "CLASS";
    public static final String FUNC = "FUNC";
    public static final String PARAM1 = "PARAM1";
    public static final String PARAM2 = "PARAM2";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String GPS = "GPS";
    public static final String GPS_CONNECT = "GPS_CONNECT";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String VIBRATION_X = "VIBRATION_X";
    public static final String VIBRATION_Y = "VIBRATION_Y";
    public static final String VIBRATION_Z = "VIBRATION_Z";
    public static final String GYRO_X = "GYRO_X";
    public static final String GYRO_Y = "GYRO_Y";
    public static final String GYRO_Z = "GYRO_Z";
    public static final String CRC = "CRC";

    /** 날짜시간*/
    public static final String DATETIME = "DATETIME";
    /** 가스누출*/
    public static final String GAS = "GAS";
    /** 방식전위*/
    public static final String CPE = "CPE";
    /** 방식전류*/
    public static final String CPA = "CPA";
    /** AC 유입*/
    public static final String AC = "AC";
    /** 수위*/
    public static final String WATER = "WATER";
    /** 배터리*/
    public static final String BATT = "BATT";
    /** 온도*/
    public static final String TEMP = "TEMP";
    /** 습도*/
    public static final String HUMI = "HUMI";

    public static final String TRANS_CTRG_ID = "00";
    public static final String UNUSED_CTRG_ID = "FF";

    /** 상태 */
    public static final String STATUS_DISABLE = "0";
    public static final String STATUS_ENABLE = "1";
    public static final String STATUS_NO = "N";
    public static final String STATUS_YES = "Y";

    /** 이벤트 등급 */
    public static final String E_NORMAL = "0";
    public static final String E_INFO = "1";
    public static final String E_WARN = "2";
    public static final String E_ERROR = "3";
    public static final String E_FATAL = "4";


    public static String suitableWord(String key) {
        String value = "";
        switch(key.toUpperCase()) {
            case "STX": value = STX; break;
            case "ETX": value = ETX; break;
            case "LENGTH": value = LENGTH; break;
            case "GROUP_ID": value = GROUP_ID; break;
            case "NODE_ID": value = NODE_ID; break;
            case "CLASS": value = CLASS; break;
            case "FUNC": value = FUNC; break;
            case "PARAM1": value = PARAM1; break;
            case "PARAM2": value = PARAM2; break;
            case "TIMESTAMP": value = TIMESTAMP; break;
            case "GPS": value = GPS; break;
            case "GPS_CONNECT": value = GPS_CONNECT; break;
            case "LATITUDE": value = LATITUDE; break;
            case "LONGITUDE": value = LONGITUDE; break;
            case "VIBRATION_X": value = VIBRATION_X; break;
            case "VIBRATION_Y": value = VIBRATION_Y; break;
            case "VIBRATION_Z": value = VIBRATION_Z; break;
            case "GYRO_X": value = GYRO_X; break;
            case "GYRO_Y": value = GYRO_Y; break;
            case "GYRO_Z": value = GYRO_Z; break;
            case "CRC": value = CRC; break;
            case "DATETIME": value = DATETIME; break;
            case "GAS": value = GAS; break;
            case "CPE": value = CPE; break;
            case "CPA": value = CPA; break;
            case "AC": value = AC; break;
            case "WATER": value = WATER; break;
            case "BATT": value = BATT; break;
            case "TEMP": value = TEMP; break;
            case "HUMI": value = HUMI; break;

            default: value = key; break;
        }

        return value;
    }
}
