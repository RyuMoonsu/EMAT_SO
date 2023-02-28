package kr.co.so.datahub.emat_so.util;

import kr.co.so.datahub.emat_so.MainService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;

/**
 * HEX 관련된 변환을 처리하는 클래스
 */
@Component
public class HexUtil {
    private static final Logger logger = LoggerFactory.getLogger(HexUtil.class);

    /**
     * HEX 에 대한 byte array 반환
     *
     * @param hexString HEX 문자열
     * @return 바이트 배열
     */
    public byte[] hexToByteArray(String hexString) {
        byte[] bytes = new byte[0];
        try {
            bytes = Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException e) {
            logger.error("hexToByteArray Exception: ", e);
        }

        return bytes;
    }

    /**
     * HEX 문자열의 Little Endian 변환
     *
     * @param hexString HEX 문자열
     * @return 문자열
     */
    public String reverseHex(String hexString) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <=hexString.length()-2; i=i+2) {
            result.append(new StringBuilder(hexString.substring(i,i+2)).reverse());
        }
        return result.reverse().toString();
    }

    /**
     * byte array 에 대한 HEX 문자열 반환
     *
     * @param byteArray byte array
     * @return 문자열
     */
    public String byteArrayToHex(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }
        return HexFormat.of().withUpperCase().formatHex(byteArray);
    }

    /**
     * HEX 형식의 날짜시간(14061D0E2627)인 경우 해당 객체로 반환
     * 오류인 경우 null 반환
     * @param dtHexString HEX 문자열
     * @return LocalDateTime
     */
    public LocalDateTime hexToDateTime(String dtHexString) {
        LocalDateTime ldt = null;
        try {
            //byte[] dtByteArray = hexToByteArray(dtHexString);
            int year = hexToInt(dtHexString.substring(0, 2)) + 2000;
            int month = hexToInt(dtHexString.substring(2, 4));
            int day = hexToInt(dtHexString.substring(4, 6));
            int hour = hexToInt(dtHexString.substring(6, 8));
            int minute = hexToInt(dtHexString.substring(8, 10));
            int second = hexToInt(dtHexString.substring(10, 12));
            ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        } catch(Exception e) {
            logger.error("LocalDateTime Exception: ", e);
        }
        return ldt;
    }

    /**
     * HEX 에 대한 문자열 반환
     * @param hexString HEX 문자열
     * @return 문자열
     */
    public String hexToString(String hexString) {
        String ret = "";
        try {
            byte[] bytes = Hex.decodeHex(hexString.toCharArray());
            ret = new String(bytes);
        } catch (DecoderException e) {
            logger.error("hexToString Exception: ", e);
        }
        return ret;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열(1 bytes)
     * @return 바이트형(Byte)
     */
    public byte hexToByte(String hexString) {
        return Byte.parseByte(hexString, 16);
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열(2 bytes)
     * @param bLittleEndian Little Endian 여부
     * @return 정수형(Short)
     */
    public short hexToShort(String hexString, boolean bLittleEndian) {
        byte[] bytes = hexToByteArray(hexString);
        short x = 0;
        if(bLittleEndian) {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } else {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
        }
        return x;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열(4 bytes)
     * @param bLittleEndian Little Endian 여부
     * @return 정수형(Integer)
     */
    public int hexToInt(String hexString, boolean bLittleEndian) {
        byte[] bytes = hexToByteArray(hexString);
        int x = 0;
        if(bLittleEndian) {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
        }
        return x;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열(8 bytes)
     * @param bLittleEndian Little Endian 여부
     * @return 정수형(Long)
     */
    public long hexToLong(String hexString, boolean bLittleEndian) {
        byte[] bytes = hexToByteArray(hexString);
        long x = 0L;
        if(bLittleEndian) {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
        }
        return x;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열
     * @return 정수형(Short)
     */
    public short hexToShort(String hexString) {
        short d = 0;
        if(StringUtils.isNotEmpty(hexString)) {
            try {
                d = Short.valueOf(hexString, 16);
            } catch(NumberFormatException e) {
                logger.error("hexToShort Exception: ", e);
            }
        }
        return d;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열
     * @return 정수형(Integer)
     */
    public int hexToInt(String hexString) {
        int d = 0;
        if(StringUtils.isNotEmpty(hexString)) {
            try {
                d = Integer.valueOf(hexString, 16);
            } catch(NumberFormatException e) {
                logger.error("hexToInt Exception: ", e);
            }
        }
        return d;
    }

    /**
     * HEX 에 대한 정수형 변환
     * @param hexString HEX 문자열
     * @return 정수형(Long)
     */
    public long hexToLong(String hexString) {
        long d = 0;
        if(StringUtils.isNotEmpty(hexString)) {
            try {
                d = Long.valueOf(hexString, 16);
            } catch(NumberFormatException e) {
                logger.error("hexToLong Exception: ", e);
            }
        }
        return d;
    }

    /**
     * 장비에서 사용하는 HEX 값의 정수형 값 반환(보수값 적용)
     * @param hexString HEX 문자열
     * @return 정수형(Integer)
     */
    public int hexToInt16 (String hexString) {
        int d = hexToInt(hexString);
        if ((d & 0x8000) > 0) {
            d = d - 0x10000;
        }
        return d;
    }

    /**
     * 장비에서 사용하는 HEX 값의 정수형 값 반환(보수값 적용)
     * @param hexString HEX 문자열
     * @return 정수형(Long)
     */
    public long hexToInt32 (String hexString) {
        long d = hexToLong(hexString);
        if ((d-0x80000000L) > 0) {
            d = d-0x100000000L;
        }
        return d;
    }

    /**
     * HEX 로부터 10진수 변환한 배열 반환
     * @param hexString HEX 문자열
     * @return 정수형(Integer) 배열
     */
    public ArrayList<Integer> hexStringToDecimalArray(String hexString) {
        ArrayList<Integer> a = new ArrayList<Integer>();
        for(int i=0; i<hexString.length();) {
            a.add(hexToInt(hexString.substring(i, i+=2)));
        }
        return a;
    }

    /**
     * HEX 에 대한 부동소수점 변환
     * @param hexString HEX 문자열(4 bytes)
     * @param bLittleEndian Little Endian 여부
     * @return 부동소수점(Float)
     */
    public float hexToFloat(String hexString, boolean bLittleEndian) {
        byte[] bytes = hexToByteArray(hexString);
        float x = 0l;
        if(bLittleEndian) {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            x = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
        }
//        Long i = Long.parseLong(hexString, 16);
//        Float x = Float.intBitsToFloat(i.intValue());
        return x;
    }

    /**
     * ASCII 값에 대한 Hex 문자열 변환
     * @param c Ascii 문자(1 byte)
     * @return Hex 문자열(String)
     */
    public String ascii2HexString(char c) {
        String hexCode=String.format("%H", c);
        return hexCode;
    }

    public byte[] shortToByteArray(short v, boolean bLittleEndian) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        if(bLittleEndian)
            bb.order(ByteOrder.LITTLE_ENDIAN);
        else
            bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(v);
        return bb.array();
    }

    public String shortToHexString(short v, boolean bLittleEndian) {
        byte[] bb = shortToByteArray(v, bLittleEndian);
        String hex = String.format("%02X%02X", bb[0], bb[1]);
        return hex;
    }

    public byte[] hexStringToByteArray(String hexString) {
        byte[] bytes = new BigInteger(hexString, 16).toByteArray();
        return bytes;
    }
}
