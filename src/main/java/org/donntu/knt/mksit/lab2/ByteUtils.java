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
}
