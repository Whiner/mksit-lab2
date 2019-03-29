package org.donntu.knt.mksit.lab2;

import java.io.*;
import java.util.Optional;

/**
 * @author Shilenko Alexander
 */
public class CompressQualifier {
    public static double compressPercent(File source, File compressed) {
        return source.length() / 100.0 * compressed.length();
    }

    public static boolean isUncompressedEqualsSource(File source, File uncompressed) {
        try (
                BufferedReader sourceBufferedReader = new BufferedReader(new FileReader(source));
                BufferedReader uncompressedBufferedReader = new BufferedReader(new FileReader(uncompressed))
        ) {
            Optional<String> sourceText = sourceBufferedReader
                    .lines()
                    .reduce((s, str) -> s.concat(str) + '\n');
            Optional<String> uncompressedText = uncompressedBufferedReader
                    .lines()
                    .reduce((s, str) -> s.concat(str) + '\n');

            if(sourceText.isPresent() && uncompressedText.isPresent()) {
                return sourceText.get().equals(uncompressedText.get());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
