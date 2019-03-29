package org.donntu.knt.mksit.lab2;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Shilenko Alexander
 */
public class Main {
    public static void main(String[] args) {
        LZ77 lz = new LZ77();
        String filename = "file.txt";
        try {
            lz.compress(filename);
            lz.unCompress(filename);
        } catch (FileNotFoundException f) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Problem processing file: " + filename);
        }
    }
}
