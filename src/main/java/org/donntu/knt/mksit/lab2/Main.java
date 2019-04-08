package org.donntu.knt.mksit.lab2;

import org.donntu.knt.mksit.lab2.v3.LZ77_v3_my;

import java.io.File;

/**
 * @author Shilenko Alexander
 */
public class Main {
    public static void main(String[] args) {


        LZ77_v3_my lz = new LZ77_v3_my(10, 10);
        String filename = "file.txt";
        String compressedFilename = "file.txt.lz77";
        String decompressedFilename = "dec_file.txt";
        try {
            lz.compress(filename, compressedFilename);
            lz.decompress(compressedFilename, decompressedFilename);
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
