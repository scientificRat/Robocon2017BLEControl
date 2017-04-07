package com.scientificrat.robocon2017blecontrol.util;

/**
 * Created by huangzhengyue on 2017/4/7.
 */

public class HexHelper {

    /**
     * 将byte数组转换为16进制形式的字符串
     *
     * @param buffer byte 数组
     * @return 16进制格式的字符串
     */
    public static String byte2hexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte aBuffer : buffer) {
            String temp = Integer.toHexString(aBuffer & 0xFF).toUpperCase();
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(" ");
            sb.append(temp);
        }
        return sb.toString().trim();
    }

    /**
     * 将16进制字符串转化为byte数组
     *
     * @param input 输入字串
     * @return byte数组
     */
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
