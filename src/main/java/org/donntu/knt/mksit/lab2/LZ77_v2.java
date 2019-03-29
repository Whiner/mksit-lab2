package org.donntu.knt.mksit.lab2;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Shilenko Alexander
 */

public class LZ77_v2 {

    // 12 bits to store maximum offset distance.
    public static final int MAX_WINDOW_SIZE = (1 << 12) - 1;

    // 4 bits to store length of the match.
    public static final int LOOK_AHEAD_BUFFER_SIZE = (1 << 4) - 1;

    // sliding window size
    private int windowSize = MAX_WINDOW_SIZE;

    public LZ77_v2(int windowSize) {
        this.windowSize = Math.min(windowSize, MAX_WINDOW_SIZE);
    }

    /**
     * Compress given input file as follows
     * <p>
     * A 0 bit followed by eight bits means just copy the eight bits to the output directly.
     * A 1 bit is followed by a pointer of 12 bits followed by a length encoded in 4 bits. This is to be interpreted as "copy the <length> bytes from <pointer> bytes ago in the output to the current location" .
     *
     * @param inputFileName  name of the input File name to be compressed
     * @param outputFileName compressed input file file will be written to
     */

    public void compress(String inputFileName, String outputFileName) throws IOException {
        BitOutputStream out = null;
        try {
            byte[] data = Files.readAllBytes(Paths.get(inputFileName));
            out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFileName)));
            for (int i = 0; i < data.length; ) {
                Match match = findMatchInSlidingWindow(data, i);
                if (match != null) {
                    out.write(Boolean.TRUE);
                    out.write((byte) (match.getDistance() >> 4));
                    out.write((byte) (((match.getDistance() & 0x0F) << 4) | match.getLength()));
                    //System.out.println("<1," + match.getDistance() + ", " + match.getLength() + ">");
                    i = i + match.getLength();
                } else {
                    out.write(Boolean.FALSE);
                    out.write(data[i]);
                    i = i + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    /**
     * decompress input file and writes to output file
     *
     * @param inputFileName  compressed input file
     * @param outputFileName decompressed output file
     * @throws IOException
     */
    public void decompress(String inputFileName, String outputFileName) throws IOException {
        BitInputStream inputFileStream = null;
        FileChannel outputChannel = null;
        RandomAccessFile outputFileStream = null;
        try {
            inputFileStream = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFileName)));
            outputFileStream = new RandomAccessFile(outputFileName, "rw");
            outputChannel = outputFileStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1);
            try {
                while (true) {// when end of file reached, inputStream throws End Of file Exception
                    int flag = inputFileStream.read();
                    if (flag == 0) {
                        buffer.clear();
                        buffer.put(inputFileStream.readByte());
                        buffer.flip();
                        outputChannel.write(buffer, outputChannel.size());
                        outputChannel.position(outputChannel.size());
                    } else {
                        int byte1 = inputFileStream.read(8);
                        int byte2 = inputFileStream.read(8);
                        int distance = (byte1 << 4) | (byte2 >> 4);
                        int length = (byte2 & 0x0f);
                        for (int i = 0; i < length; i++) {
                            buffer.clear();
                            outputChannel.read(buffer, outputChannel.position() - distance);
                            buffer.flip();
                            outputChannel.write(buffer, outputChannel.size());
                            outputChannel.position(outputChannel.size());
                        }
                    }
                }
            } catch (EOFException e) {
                // ignore. means we reached the end of the file. and we are done.
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputFileStream.close();
            outputChannel.close();
            inputFileStream.close();
        }
    }

    private Match findMatchInSlidingWindow(byte[] data, int currentIndex) {
        Match match = new Match();
        int end = Math.min(currentIndex + LOOK_AHEAD_BUFFER_SIZE, data.length + 1);
        for (int j = currentIndex + 2; j < end; j++) {
            int startIndex = Math.max(0, currentIndex - windowSize);
            byte[] bytesToMatch = Arrays.copyOfRange(data, currentIndex, j);
            for (int i = startIndex; i < currentIndex; i++) {
                int repeat = bytesToMatch.length / (currentIndex - i);
                int remaining = bytesToMatch.length % (currentIndex - i);

                byte[] tempArray = new byte[(currentIndex - i) * repeat + (i + remaining - i)];
                int m = 0;
                for (; m < repeat; m++) {
                    int destPos = m * (currentIndex - i);
                    System.arraycopy(data, i, tempArray, destPos, currentIndex - i);
                }
                int destPos = m * (currentIndex - i);
                System.arraycopy(data, i, tempArray, destPos, remaining);
                if (Arrays.equals(tempArray, bytesToMatch) && bytesToMatch.length > match.getLength()) {
                    match.setLength(bytesToMatch.length);
                    match.setDistance(currentIndex - i);
                }
            }
        }
        if (match.getLength() > 0 && match.getDistance() > 0)
            return match;
        return null;
    }
}
