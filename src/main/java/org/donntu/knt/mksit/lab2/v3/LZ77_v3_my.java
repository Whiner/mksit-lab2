package org.donntu.knt.mksit.lab2.v3;

import java.io.BufferedWriter;
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
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))
        ) {
            Buffer window = new Buffer();
            Buffer buffer = new Buffer();
            bufferedWriter.write(randomAccessFile.readByte());
            while (moveWindowAndBuffer(window, buffer, randomAccessFile, 1)) {
                Match match = checkMatches(window, buffer);
                if (match != null) {
                    bufferedWriter.write('<' + match.getOffset() + ';' + match.getLength() + '>');
                    moveWindowAndBuffer(window, buffer, randomAccessFile, match.getLength());
                } else {
                    bufferedWriter.write(buffer.getBuffer().charAt(0));
                }
            }

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
            int bufferLength = window.getBuffer().length();
            file.seek(window.getStartBufferPosition() + bufferLength);

            byte[] byteBuffer = new byte[step];
            file.read(byteBuffer);
            window.getBuffer().append(new String(byteBuffer));

            if (bufferLength > MAX_WINDOW_SIZE) {
                int deletingCount = bufferLength - MAX_WINDOW_SIZE;
                for (int i = 0; i < deletingCount; i++) {
                    window.deleteCharAt(0);
                }
            }

            if (buffer.getBuffer().length() == 0) {
                initializeBuffer(buffer, file);
            } else {
                file.seek(buffer.getStartBufferPosition() + buffer.getBuffer().length());
                file.read(byteBuffer);
                buffer.getBuffer().append(new String(byteBuffer));
                for (int i = 0; i < step; i++) {
                    buffer.deleteCharAt(0);
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private Match checkMatches(Buffer window, Buffer buffer) {
        StringBuilder matchString = new StringBuilder();
        char[] windowChars = window.getBuffer().toString().toCharArray();
        int currentTextPosition = 0;
        Match match = null;
        while (currentTextPosition < windowChars.length) {
            final char aChar = windowChars[currentTextPosition];
            if (aChar == buffer.getBuffer().charAt(currentTextPosition)) {
                matchString.append(aChar);
            } else if (currentTextPosition != 0) {
                break;
            }
            currentTextPosition++;
        }
        if (currentTextPosition > 1) {
            match = new Match(
                    window.getStartBufferPosition() + currentTextPosition,
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
