package com.scientificrat.robocon2017blecontrol.util;

/**
 * Created by huangzhengyue on 2017/4/7.
 */

public class HexHelper {
    public static String byte2hexString(byte[] buffer) {
        String h = "";
        for (byte aBuffer : buffer) {
            String temp = Integer.toHexString(aBuffer & 0xFF).toUpperCase();
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }
        return h;
    }

    public static byte[] hexString2byte(String input) {
        int length = input.length();
        int byteSize;
        if (length % 2 == 0) {
            byteSize = length / 2;
        } else {
            byteSize = length / 2 + 1;
            input = "0" + input;
        }
        byte[] rst = new byte[byteSize];
        for (int i = 0; i < byteSize; i++) {
            rst[i] = Byte.parseByte(input.substring(i * 2, i * 2 + 2), 16);
        }
        return rst;

    }
}
