package org.donntu.knt.mksit.lab2.v3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LZ77_v3_my {
    private final int MAX_WINDOW_SIZE;
    private final int BUFFER_SIZE;

    public LZ77_v3_my(int maxWindowSize, int bufferSize) {
        this.MAX_WINDOW_SIZE = maxWindowSize;
        this.BUFFER_SIZE = bufferSize;
    }

    public void compress(String inputFileName, String outputFileName) {
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(inputFileName, "r");
                FileWriter bufferedWriter = new FileWriter(outputFileName)
        ) {
            Buffer window = new Buffer();
            Buffer buffer = new Buffer();
            bufferedWriter.write(randomAccessFile.readByte());
            int step = 1;

            while (moveWindowAndBuffer(window, buffer, randomAccessFile, step)) {
                step = 1;
                Match match = checkMatches(window, buffer);
                if (match != null) {
                    bufferedWriter.write("<" + match.getOffset() + ";" + match.getLength() + ">");
                    step = match.getLength();
                } else {
                    bufferedWriter.write(buffer.getBuffer().charAt(0));
                }
            }
            StringBuffer stringBuffer = buffer.getBuffer();
            stringBuffer.deleteCharAt(0);
            bufferedWriter.write(stringBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeBuffer(Buffer buffer, RandomAccessFile file) throws IOException {
        buffer.getBuffer().setLength(0);
        file.seek(1);
        byte[] byteBuffer = new byte[BUFFER_SIZE];
        file.read(byteBuffer);
        buffer.getBuffer().append(new String(byteBuffer));
        buffer.setStartBufferPosition(1);
    }

    private boolean moveWindowAndBuffer(Buffer window, Buffer buffer, RandomAccessFile file, int step) {
        try {
            file.seek(window.getStartBufferPosition() + window.getBuffer().length());

            byte[] byteBuffer = new byte[step];
            file.read(byteBuffer);
            window.getBuffer().append(new String(byteBuffer));

            int bufferLength = window.getBuffer().length();
            if (bufferLength > MAX_WINDOW_SIZE) {
                int deletingCount = bufferLength - MAX_WINDOW_SIZE;
                for (int i = 0; i < deletingCount; i++) {
                    window.deleteCharAt(0);
                }
            }

            if (buffer.getBuffer().length() == 0) {
                initializeBuffer(buffer, file);
            } else {
                file.seek(buffer.getStartBufferPosition() + buffer.getBuffer().length() + step);
                if (file.read() == -1) {
                    throw new IOException("File end");
                }

                file.seek(buffer.getStartBufferPosition() + buffer.getBuffer().length());
                file.read(byteBuffer);
                buffer.getBuffer().append(new String(byteBuffer));
                for (int i = 0; i < step; i++) {
                    buffer.deleteCharAt(0);
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Match checkMatches(Buffer window, Buffer buffer) {
        StringBuilder matchString = new StringBuilder();
        char[] windowChars = window.getBuffer().toString().toCharArray();
        Match match = null;
        int windowI = 0;
        while (windowI < windowChars.length) {
            final char aChar = windowChars[windowI];
            if (aChar == buffer.getBuffer().charAt(windowI)) {
                matchString.append(aChar);
            } else if (windowI != 0) {
                break;
            }
            windowI++;
        }
        if (windowI > 1) {
            match = new Match(
                    window.getStartBufferPosition() + windowI - matchString.length(),
                    matchString.length()
            );
        }
        return match;
    }

    /*private boolean fillBufferToMaxSize(Buffer buffer, RandomAccessFile file) {
        try {
            StringBuffer stringBuffer = buffer.getBuffer();
            byte[] byteBuffer = new byte[BUFFER_SIZE - stringBuffer.length()];
            file.seek(buffer.getStartBufferPosition() + stringBuffer.length());
            file.read(byteBuffer);
            stringBuffer.append(new String(byteBuffer));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }*/
}
