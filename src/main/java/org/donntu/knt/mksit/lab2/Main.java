package org.donntu.knt.mksit.lab2;

import java.io.File;

/**
 * @author Shilenko Alexander
 */
public class Main {
    public static void main(String[] args) {


        LZ77_v2 lz = new LZ77_v2(100);
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
