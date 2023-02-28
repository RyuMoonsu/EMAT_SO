package kr.co.so.datahub.emat_so.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.co.so.datahub.emat_so.Constants;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component
public class Parser {
    private static int TWO_DIGIT = 2;
    private static CrcUtil crcUtil;
    private static HexUtil hexUtil;
    private boolean CRC_CHECK = true;
    private static JsonObject protocolObject;
    private static boolean crcCheck;

    public Parser() {
        new Parser(null, null, null);
    }

    public Parser(JsonObject protocolObject, CrcUtil crcUtil, HexUtil hexUtil) {
        new Parser(protocolObject, crcUtil, hexUtil, true);
    }

    public Parser(JsonObject protocolObject, CrcUtil crcUtil, HexUtil hexUtil, boolean crcCheck) {
        this.protocolObject = protocolObject;
        this.crcUtil = crcUtil;
        this.hexUtil = hexUtil;
        this.crcCheck = crcCheck;
    }

    public String parse(String hexString) throws DecoderException {
        return parse(hexString, "");
    }

    public String parse(String hexString, String modelName) throws DecoderException {
        //hexString = StringUtils.trim(hexString);
        Gson gson = new Gson();
        JsonObject resultJo = new JsonObject();
        int idx = 0;
        String key = "";
        String fpp = "";
        List<String> keyList = Arrays.asList(protocolObject.get("KEY-ORDER").getAsString().split(","));
        resultJo.addProperty("_IDX_", idx);
        //System.out.println(hexString);
        switch(modelName) {
            default:
                if(StringUtils.isNotEmpty(hexString)) {
                    for(int kdx=0; kdx<keyList.size(); kdx++) {
                        key = keyList.get(kdx);
                        JsonObject jo = null;

                        if(StringUtils.equalsIgnoreCase(key,"RAW_DATA")) {
                            jo = protocolObject.getAsJsonObject(key);
                            resultJo = getRawDataObject(resultJo, hexString, key, jo);
                        } else {
                            jo = protocolObject.getAsJsonObject(key);
                            resultJo = getObjectValue(resultJo, hexString, key, jo);
                        }
                        if(null != resultJo.get("_FAILURE_")) {
                            break;
                        }
                    } // for FOR statement
                }
                break; // for CASE break
        }
        resultJo.remove("_IDX_");

        return gson.toJson(resultJo);
    }

    private JsonObject appendJsonObject(JsonObject jo, String javaType, String key, String value) {
        if (StringUtils.equalsIgnoreCase(javaType, "LITERAL")) {
            jo.addProperty(Constants.suitableWord(key), value);
        } else if (StringUtils.equalsIgnoreCase(javaType, "STRING")) {
            jo.addProperty(Constants.suitableWord(key), value);
        } else if (StringUtils.equalsIgnoreCase(javaType, "CHAR")) {
            jo.addProperty(Constants.suitableWord(key), value);
        } else if (StringUtils.equalsIgnoreCase(javaType, "BYTE")) {
            jo.addProperty(Constants.suitableWord(key), Byte.valueOf(value));
        } else if (StringUtils.equalsIgnoreCase(javaType, "SHORT")) {
            jo.addProperty(Constants.suitableWord(key), Short.valueOf(value));
        } else if (StringUtils.equalsIgnoreCase(javaType, "INT")) {
            jo.addProperty(Constants.suitableWord(key), Integer.valueOf(value));
        } else if (StringUtils.equalsIgnoreCase(javaType, "LONG")) {
            jo.addProperty(Constants.suitableWord(key), Long.valueOf(value));
        } else if (StringUtils.equalsIgnoreCase(javaType, "DATETIME")) {
            jo.addProperty(Constants.suitableWord(key), value);
        } else if (StringUtils.equalsIgnoreCase(javaType, "FLOAT")) {
            jo.addProperty(Constants.suitableWord(key), Float.valueOf(value));
        }
        return jo;
    }

    private JsonObject getObjectValue(JsonObject resultJo, String hexString, String key, JsonObject protocolJo) {
        try {
            int idx = resultJo.get("_IDX_").getAsInt();
            int hexLen = protocolJo.get("BYTE").getAsInt();
            String fwType = protocolJo.get("FW_TYPE").getAsString();
            String javaType = protocolJo.get("JAVA_TYPE").getAsString();
            String isLe = protocolJo.get("LE").getAsString();
            String subHexString = hexString.substring(idx, idx += TWO_DIGIT * hexLen);
            String resultValue = resultValue(subHexString, fwType, javaType, isLe);

            boolean isSuccess = true;
            String reason = "";

            if (CRC_CHECK && StringUtils.equalsIgnoreCase(key, "CRC")) {
                int extidx = StringUtils.lastIndexOf(hexString, "03");
                String crcHex = StringUtils.substring(hexString, 0, extidx - (TWO_DIGIT * 2));
                String hashCRC = crcUtil.makeCRC16(hexUtil.hexToByteArray(crcHex));

                if (!StringUtils.equalsIgnoreCase(resultValue, hashCRC)) {
                    isSuccess = false;
                    reason = "The CRC is mismatch(" + resultValue + ":" + hashCRC + ")";
                }
            }

            if (isSuccess) {
                resultJo = appendJsonObject(resultJo, javaType, key, resultValue);
            } else {
                resultJo.addProperty("_FAILURE_", reason);
            }

            resultJo.addProperty("_IDX_", idx);
        } catch(Exception e) {
            resultJo.addProperty("_FAILURE_", e.getMessage());
        }
        return resultJo;
    }

    private JsonObject getRawDataObject(JsonObject resultJo, String hexString, String rootKey, JsonObject protocolJo) throws DecoderException {
        Iterator<String> iterator = protocolJo.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            JsonObject jo = protocolJo.getAsJsonObject(key);
            resultJo = getObjectValue(resultJo, hexString, key, jo);
        }

        return resultJo;
    }

    private String resultValue(String subHexString, String fwType, String javaType, String isLe) {
        String result = "";
        String vString = "";
        byte vByte = 0;
        short vShort = 0;
        int vInt = 0;
        long vLong = 0l;
        float vFloat = 0f;

        if (StringUtils.equalsIgnoreCase(fwType, "UINT8") || StringUtils.equalsIgnoreCase(fwType, "CHAR")) { // Ignore "LE", because of ONLY "1" byte
            if (StringUtils.equalsIgnoreCase(javaType, "LITERAL")) {
                result = String.valueOf(subHexString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "STRING")) {
                vString = subHexString;
                result = String.valueOf(vString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "CHAR")) {
                result = String.valueOf((char) Integer.parseInt(subHexString, 16));
            } else if (StringUtils.equalsIgnoreCase(javaType, "BYTE")) {
                vByte = hexUtil.hexToByte(subHexString);
                result = String.valueOf(vByte);
            } else if (StringUtils.equalsIgnoreCase(javaType, "SHORT")) {
                vShort = hexUtil.hexToShort(subHexString);
                result = String.valueOf(vShort);
            }
        } else if (StringUtils.equalsIgnoreCase(fwType, "UINT16")) {
            if (StringUtils.equalsIgnoreCase(javaType, "SHORT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    vShort = hexUtil.hexToShort(subHexString, true);
                    result = String.valueOf(vShort);
                } else {
                    vShort = hexUtil.hexToShort(subHexString, false);
                    result = String.valueOf(vShort);
                }
            } else if (StringUtils.equalsIgnoreCase(javaType, "INT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    subHexString = hexUtil.reverseHex(subHexString);
                }
                vInt = hexUtil.hexToShort(subHexString);
                result = String.valueOf(vInt);
            }
        } else if (StringUtils.equalsIgnoreCase(fwType, "INT16")) {
            if (StringUtils.equalsIgnoreCase(javaType, "SHORT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    vShort = hexUtil.hexToShort(subHexString, true);
                    result = String.valueOf(vShort);
                } else {
                    vShort = hexUtil.hexToShort(subHexString, false);
                    result = String.valueOf(vShort);
                }
            } else if (StringUtils.equalsIgnoreCase(javaType, "INT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    subHexString = hexUtil.reverseHex(subHexString);
                }
                vInt = hexUtil.hexToInt(subHexString);
                result = String.valueOf(vInt);
            } else if (StringUtils.equalsIgnoreCase(javaType, "STRING")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    subHexString = hexUtil.reverseHex(subHexString);
                }
                vString = subHexString;
                result = String.valueOf(vString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "LITERAL")) {
                result = String.valueOf(subHexString);
            }
        } else if (StringUtils.equalsIgnoreCase(fwType, "INT32")) {
            if (StringUtils.equalsIgnoreCase(javaType, "INT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    vInt = hexUtil.hexToInt(subHexString, true);
                    result = String.valueOf(vInt);
                } else {
                    vInt = hexUtil.hexToInt(subHexString, false);
                    result = String.valueOf(vInt);
                }
            } else if(StringUtils.equalsIgnoreCase(javaType, "LONG")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    subHexString = hexUtil.reverseHex(subHexString);
                }
                vLong = hexUtil.hexToLong(subHexString);
                result = String.valueOf(vLong);
            } else if (StringUtils.equalsIgnoreCase(javaType, "STRING")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    subHexString = hexUtil.reverseHex(subHexString);
                }
                vString = subHexString;
                result = String.valueOf(vString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "LITERAL")) {
                result = String.valueOf(subHexString);
            }
        } else if (StringUtils.equalsIgnoreCase(fwType, "BYTEARRAY")) {
            if (StringUtils.equalsIgnoreCase(javaType, "LITERAL")) {
                result = String.valueOf(subHexString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "STRING")) {
                vString = subHexString;
                result = String.valueOf(vString);
            } else if (StringUtils.equalsIgnoreCase(javaType, "DATETIME")) {
                LocalDateTime ldt = hexUtil.hexToDateTime(subHexString);
                if(null != ldt) {
                    vString = ldt.toString();
                    result = String.valueOf(vString);
                } else {
                    vString = Constants.BASE_DATETIME;
                    result = String.valueOf(vString);
                }
            }
        } else if (StringUtils.equalsIgnoreCase(fwType, "FLOAT")) {
            if (StringUtils.equalsIgnoreCase(javaType, "FLOAT")) {
                if (StringUtils.equalsIgnoreCase(isLe, "YES")) {
                    vFloat = hexUtil.hexToFloat(subHexString, true);
                    result = String.valueOf(vFloat);
                } else {
                    vFloat = hexUtil.hexToFloat(subHexString, false);
                    result = String.valueOf(vFloat);
                }
            }
        }

        return result;
    }
}
