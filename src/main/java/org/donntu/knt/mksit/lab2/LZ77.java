package org.donntu.knt.mksit.lab2;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static org.donntu.knt.mksit.lab2.ByteUtils.byteToBits;

/**
 * @author Shilenko Alexander
 */

public class LZ77 {
    private final int MAX_WINDOW_SIZE = (1 << 6) - 1;
    private final int BUFFER_SIZE = (1 << 3) - 1;

    private final int OFFSET_LENGTH = (int) Math.ceil(Math.log(MAX_WINDOW_SIZE) / Math.log(2));
    private final int LENGTH_LENGTH = (int) Math.ceil(Math.log(BUFFER_SIZE) / Math.log(2));
    private final int CHAR_LENGTH = (int) Math.ceil(Math.log(255) / Math.log(2));

    public String compress(String inputFileName) {
        String outputFileName = getNameCompressedFile(inputFileName);
        new File(outputFileName).delete();
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(inputFileName, "r");
                FileOutputStream writer = new FileOutputStream(outputFileName)
        ) {
            List<Byte> window = new LinkedList<>();
            List<Byte> buffer = new LinkedList<>();
            StringBuilder binaryBuffer = new StringBuilder();
            initializeBuffer(buffer, randomAccessFile);
            int step;
            do {
                Match match = checkMatch(window, buffer);
                binaryBuffer.append(matchToBits(match));
                while (binaryBuffer.length() >= 8) {
                    writer.write(Integer.parseInt(binaryBuffer.substring(0, 8), 2));
                    binaryBuffer.delete(0, 8);
                }
                step = match.getLength() == 0 ? 1 : match.getLength();
            }
            while (moveWindowAndBuffer(window, buffer, randomAccessFile, step));

            while (binaryBuffer.length() > 0) {
                if (binaryBuffer.length() < 8) {
                    int zeroCount = 8 - binaryBuffer.length();
                    for (int i = 0; i < zeroCount; i++) {
                        binaryBuffer.append('0');
                    }
                }
                writer.write(Integer.parseInt(binaryBuffer.substring(0, 8), 2));
                binaryBuffer.delete(0, 8);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFileName;
    }

    private String matchToBits(Match match) {
        return byteToBits((byte) match.getOffset(), OFFSET_LENGTH) +
                byteToBits((byte) match.getLength(), LENGTH_LENGTH) +
                byteToBits(match.getNextByte(), CHAR_LENGTH);
    }

    private void initializeBuffer(List<Byte> buffer, RandomAccessFile file) throws IOException {
        buffer.clear();
        file.seek(0);
        long length = file.length();
        byte[] byteBuffer = new byte[BUFFER_SIZE > length ? (int) length - 1 : BUFFER_SIZE];
        file.read(byteBuffer);
        for (byte b : byteBuffer) {
            buffer.add(b);
        }
    }

    private boolean moveWindowAndBuffer(List<Byte> window, List<Byte> buffer, RandomAccessFile file, int step) throws IOException {

        for (int i = 0; i < step; i++) {
            window.add(buffer.remove(0));
        }

        if (buffer.isEmpty()) {
            return false;
        }

        if (window.size() > MAX_WINDOW_SIZE) {
            shortenBuffer(window, window.size() - MAX_WINDOW_SIZE);
        }

        if (file.getFilePointer() < file.length()) {
            for (int i = 0; i < step; i++) {
                try {
                    buffer.add(file.readByte());
                } catch (IOException ignored) {
                }
            }
        }

        return true;

    }

    private void shortenBuffer(List<Byte> buffer, int count) {
        if (!buffer.isEmpty() && count > 0) {
            buffer.subList(0, count).clear();
        }
    }

    private Match checkMatch(List<Byte> window, List<Byte> buffer) {
        int byteMatchesCount = 0;
        int windowOffset;
        int bufferOffset = 0;


        for (windowOffset = 0; windowOffset < window.size(); windowOffset++) {
            byte windowByte = window.get(windowOffset);
            try {
                if (windowByte == buffer.get(bufferOffset) && bufferOffset != buffer.size() - 1) {
                    byteMatchesCount++;
                    bufferOffset++;
                } else if (byteMatchesCount != 0) {
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        if (byteMatchesCount != 0) {
            byte nextByte = buffer.get(byteMatchesCount);
            return new Match(
                    windowOffset - byteMatchesCount,
                    byteMatchesCount,
                    nextByte
            );
        } else {
            return new Match(1, 0, buffer.get(0));
        }
    }

    public String decompress(String inputFileName) {
        String outputFileName = getNameDecompressedFile(inputFileName);
        new File(outputFileName).delete();
        try (
                RandomAccessFile file = new RandomAccessFile(inputFileName, "r");
                FileOutputStream writer = new FileOutputStream(outputFileName)
        ) {
            StringBuilder binaryBuffer = new StringBuilder();
            List<Byte> window = new LinkedList<>();
            final int matchSize = OFFSET_LENGTH + LENGTH_LENGTH + CHAR_LENGTH;
            while (file.getFilePointer() < file.length()) {
                while (binaryBuffer.length() < matchSize) {
                    binaryBuffer.append(byteToBits(file.readByte(), 8));
                }
                int offset = Integer.parseInt(binaryBuffer.substring(0, OFFSET_LENGTH), 2);
                binaryBuffer.delete(0, OFFSET_LENGTH);
                int length = Integer.parseInt(binaryBuffer.substring(0, LENGTH_LENGTH), 2);
                binaryBuffer.delete(0, LENGTH_LENGTH);
                int nextByte = Integer.parseInt(binaryBuffer.substring(0, CHAR_LENGTH), 2);
                binaryBuffer.delete(0, CHAR_LENGTH);

                if (length == 0) {
                    window.add((byte) nextByte);
                    writer.write(nextByte);
                } else {
                    for (int i = offset; i < offset + length; i++) {
                        Byte e = window.get(i);
                        window.add(e);
                        writer.write(e);
                    }
                }

                if (window.size() > MAX_WINDOW_SIZE) {
                    window.subList(0, window.size() - MAX_WINDOW_SIZE).clear();
                }
            }

        } catch (Exception e) {
            System.out.println("Completed");
        }
        return outputFileName;
    }

    private String getNameCompressedFile(String filename) {
        return filename + ".lz77";
    }

    private String getNameDecompressedFile(String filename) {
        String[] split = filename.split("/");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            stringBuilder.append(split[i]).append('/');
        }
        stringBuilder.append("decoded_").append(split[split.length - 1]);
        return stringBuilder.toString().replace(".lz77", "");
    }
}
