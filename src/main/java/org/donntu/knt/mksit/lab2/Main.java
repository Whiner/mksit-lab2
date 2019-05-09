package org.donntu.knt.mksit.lab2;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Shilenko Alexander
 */
public class Main {
    public static void main(String[] args) {

        /*List<Byte> buffer = Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5);
        List<Byte> window = Arrays.asList((byte)7, (byte)8, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5);*/
        //lz.check(window, buffer);
        LZ77 lz = new LZ77();
        //String filename = "files/file.txt";
        String filename = "files/image2.bmp";
        try {
            String compressedFilename = lz.compress(filename);
            String decompressedFilename = lz.decompress(compressedFilename);
            System.out.println("compress percent = " + CompressQualifier.compressPercent(
                    new File(filename),
                    new File(compressedFilename))
            );
            System.out.println("is equals = " + CompressQualifier.isUncompressedEqualsSource(
                    new File(filename),
                    new File(decompressedFilename))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
