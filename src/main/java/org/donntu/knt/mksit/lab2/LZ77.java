package org.donntu.knt.mksit.lab2;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.donntu.knt.mksit.lab2.ByteUtils.byteListToByteArray;
import static org.donntu.knt.mksit.lab2.ByteUtils.byteToBits;

public class LZ77 {
    private final int MAX_WINDOW_SIZE = (1 << 5) - 1;
    private final int BUFFER_SIZE = (1 << 3) - 1;

    private final int OFFSET_LENGTH = (int) Math.ceil(Math.log1p(MAX_WINDOW_SIZE));
    private final int LENGTH_LENGTH = (int) Math.ceil(Math.log1p(BUFFER_SIZE));
    private final int CHAR_LENGTH = 8;

    public void compress(String inputFileName, String outputFileName) {
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(inputFileName, "r");
                FileOutputStream fileOutputStream = new FileOutputStream(outputFileName)
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
                    fileOutputStream.write(Integer.parseInt(binaryBuffer.substring(0, 8), 2));
                    binaryBuffer.delete(0, 8);
                }
                step = match.getLength() == 0 ? 1 : match.getLength();
            }
            while (moveWindowAndBuffer(window, buffer, randomAccessFile, step));

            while (binaryBuffer.length() > 0) {
                if(binaryBuffer.length() < 8) {
                    int zeroCount = 8 - binaryBuffer.length();
                    for (int i = 0; i < zeroCount; i++) {
                        binaryBuffer.append('0');
                    }
                }
                fileOutputStream.write(Integer.parseInt(binaryBuffer.substring(0, 8), 2));
                binaryBuffer.delete(0, 8);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        try {
            for (int i = 0; i < step; i++) {
                window.add(buffer.remove(0));
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        if (window.size() > MAX_WINDOW_SIZE) {
            shortenBuffer(window, window.size() - MAX_WINDOW_SIZE);
        }

        if(file.getFilePointer() < file.length()) {
            for (int i = 0; i < step; i++) {
                buffer.add(file.readByte());
            }
        }

        return true;

    }

    private boolean shortenBuffer(List<Byte> buffer, int count) {
        if (buffer.size() == 0) {
            return false;
        }
        if (count > 0) {
            buffer.subList(0, count).clear();
        }
        return true;
    }

    private Match checkMatch(List<Byte> window, List<Byte> buffer) {
        List<Byte> byteMatches = new LinkedList<>();
        int windowOffset;
        int bufferOffset = 0;


        for (windowOffset = 0; windowOffset < window.size(); windowOffset++) {
            byte windowByte = window.get(windowOffset);
            try {
                if (windowByte == buffer.get(bufferOffset) && bufferOffset != buffer.size() - 1) {
                    byteMatches.add(windowByte);
                    bufferOffset++;
                } else if (byteMatches.size() != 0) {
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        if (!byteMatches.isEmpty()) {
            byte nextByte = buffer.get(byteMatches.size());

            return new Match(
                    windowOffset - byteMatches.size(),
                    byteMatches.size(),
                    nextByte
            );
        } else {
            return new Match(1, 0, buffer.get(0));
        }
    }

    public void decompress(String inputFileName, String outputFileName) {
        new File(outputFileName).delete();
        try (
                RandomAccessFile file = new RandomAccessFile(inputFileName, "r");
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))
        ) {
            StringBuilder binaryBuffer = new StringBuilder();
            List<Byte> window = new LinkedList<>();
            final int matchSize = OFFSET_LENGTH + LENGTH_LENGTH + CHAR_LENGTH;
            while(file.getFilePointer() <= file.length()) {
                while(binaryBuffer.length() < matchSize) {
                    binaryBuffer.append(byteToBits(file.readByte(), 8));
                }
                int offset = Integer.parseInt(binaryBuffer.substring(0, OFFSET_LENGTH), 2);
                binaryBuffer.delete(0, OFFSET_LENGTH);
                int length = Integer.parseInt(binaryBuffer.substring(0, LENGTH_LENGTH), 2);
                binaryBuffer.delete(0, LENGTH_LENGTH);
                int nextByte = Integer.parseInt(binaryBuffer.substring(0, CHAR_LENGTH), 2);
                binaryBuffer.delete(0, CHAR_LENGTH);

                if(length == 0) {
                    window.add((byte) nextByte);
                    bufferedWriter.write(nextByte);
                } else {
                    for (int i = offset; i < offset + length; i++) {
                        Byte e = window.get(i);
                        window.add(e);
                        bufferedWriter.write(e);
                    }
                }

                if(window.size() > MAX_WINDOW_SIZE) {
                    window.subList(0, window.size() - MAX_WINDOW_SIZE).clear();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
