package org.donntu.knt.mksit.lab2;

import org.donntu.knt.mksit.lab2.v4.LZ77ByteEdition;

import java.io.File;

/**
 * @author Shilenko Alexander
 */
public class Main {
    public static void main(String[] args) {

        //TODO: обрезать буффер по размеру файла. когда инициализируется буфер просто удалять все ненужное
        //LZ77 lz = new LZ77(30,30);
        LZ77ByteEdition lz = new LZ77ByteEdition(150,150);
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
