package org.donntu.knt.mksit.lab2;

import java.util.List;

public class ByteUtils {
    public static byte[] byteListToByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    public static String byteToBits(byte b, int size) {
        return String.format("%" + size + "s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');
    }

    public static String intToBits(int b, int size) {
        return String.format("%" + size + "s", Integer.toBinaryString(b))
                .replace(' ', '0');
    }
}
