package org.donntu.knt.mksit.lab2;

import java.io.*;

public class LZ77 {
    private final int MAX_WINDOW_SIZE;
    private final int BUFFER_SIZE;

    public LZ77(int maxWindowSize, int bufferSize) {
        this.MAX_WINDOW_SIZE = maxWindowSize;
        this.BUFFER_SIZE = bufferSize;
    }

    public void compress(String inputFileName, String outputFileName) {
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(inputFileName, "r");
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(outputFileName)
                        )
                )
        ) {
            Buffer window = new Buffer();
            Buffer buffer = new Buffer();
            byte firstByte = randomAccessFile.readByte();
            window.getBuffer().append((char) firstByte);
            bufferedWriter.write(firstByte);
            initializeBuffer(buffer, randomAccessFile);
            int step;
            do {
                step = 1;
                Match match = checkMatches(window, buffer);
                if (match != null) {
                    bufferedWriter.write("<" + match.getOffset() + ";" + match.getLength() + ">");
                    step = match.getLength();
                } else {
                    if (buffer.getBuffer().length() == 0) {
                        return;
                    } else {
                        bufferedWriter.write(buffer.getBuffer().charAt(0));
                    }
                }
            } while (moveWindowAndBuffer(window, buffer, randomAccessFile, step));
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
            byte[] byteBuffer = new byte[step];
            file.seek(window.getStartBufferPosition() + window.length());
            file.read(byteBuffer);
            window.getBuffer().append(new String(byteBuffer));
            if(window.length() > MAX_WINDOW_SIZE) {
                shortenBuffer(window, window.length() - MAX_WINDOW_SIZE);
            }

            int i;
            for (i = step; i >= 0; i--) {
                file.seek(buffer.getStartBufferPosition() + buffer.length() + i);
                if(file.read() != -1) {
                    break;
                }
            }
            if(i == 0) {
                if(!shortenBuffer(buffer, step)){
                    throw new IOException("File end");
                }
            } else {
                if (byteBuffer.length != i) {
                    byteBuffer = new byte[i];
                }

                file.seek(buffer.getStartBufferPosition() + buffer.length());
                file.read(byteBuffer);
                buffer.getBuffer().append(new String(byteBuffer));
                shortenBuffer(buffer, step);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean shortenBuffer(Buffer buffer, int count) {
        if(buffer.length() == 0) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            buffer.deleteCharAt(0);
        }
        return true;
    }

    private Match checkMatches(Buffer window, Buffer buffer) {
        StringBuilder matchString = new StringBuilder();
        char[] windowChars = window.getBuffer().toString().toCharArray();
        Match match = null;
        int windowOffset;
        int bufferOffset = 0;
        for (windowOffset = 0; windowOffset < windowChars.length; windowOffset++) {
            char windowChar = windowChars[windowOffset];
            try {
                if (windowChar == buffer.getBuffer().charAt(bufferOffset)) {
                    matchString.append(windowChar);
                    bufferOffset++;
                } else if (matchString.length() != 0) {
                    break;
                }
            } catch (StringIndexOutOfBoundsException e) {
                break;
            }
        }
        if (matchString.length() > 1) {
            match = new Match(
                    window.getStartBufferPosition() + windowOffset - matchString.length(),
                    matchString.length()
            );
        }
        return match;
    }

    public void decompress(String inputFileName, String outputFileName) {
        new File(outputFileName).delete();
        try (
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(inputFileName)
                        )
                );
                RandomAccessFile randomAccessFile = new RandomAccessFile(outputFileName, "rw")
        ) {
            char character;
            int read;
            while ((read = bufferedReader.read()) != -1) {
                character = (char) read;
                if (character != '<') {
                    randomAccessFile.write(character);
                } else {
                    Match match = readCodeBlock(bufferedReader);
                    byte[] buffer = new byte[match.getLength()];
                    long endPointer = randomAccessFile.getFilePointer();
                    randomAccessFile.seek(match.getOffset());
                    randomAccessFile.read(buffer);
                    randomAccessFile.seek(endPointer);
                    randomAccessFile.write(buffer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Match readCodeBlock(BufferedReader bufferedReader) throws IOException {
        char character;
        Match match = new Match();

        StringBuilder buffer = new StringBuilder();

        while ((character = (char) bufferedReader.read()) != '>') {
            if (character != ';') {
                buffer.append(character);
            } else {
                match.setOffset(Integer.valueOf(buffer.toString()));
                buffer.setLength(0);
            }
        }

        match.setLength(Integer.valueOf(buffer.toString()));

        return match;
    }
}
