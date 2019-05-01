package org.donntu.knt.mksit.lab2;

import java.io.File;

/**
 * @author Shilenko Alexander
 */
public class Main {
    //123123456123456666612312345612345666661231234561234566666
    //6612312345612345666669
    public static void main(String[] args) {

        LZ77 lz = new LZ77();
        String filename = "files/file.txt";
        String compressedFilename = "files/file.txt.lz77";
        String decompressedFilename = "files/dec_file.txt";
        /*String filename = "files/image2.bmp";
        String compressedFilename = "files/image.bmp.lz77";
        String decompressedFilename = "files/dec_image.bmp";*/
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
