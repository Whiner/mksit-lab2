package org.donntu.knt.mksit.lab2.v4;

import org.donntu.knt.mksit.lab2.Match;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.donntu.knt.mksit.lab2.ByteUtils.byteListToByteArray;

public class LZ77ByteEdition {
    private final int MAX_WINDOW_SIZE;
    private final int BUFFER_SIZE;

    public LZ77ByteEdition(int maxWindowSize, int bufferSize) {
        this.MAX_WINDOW_SIZE = maxWindowSize;
        this.BUFFER_SIZE = bufferSize;
    }

    public void compress(String inputFileName, String outputFileName) {
        try (
                RandomAccessFile randomAccessFile = new RandomAccessFile(inputFileName, "r");
                FileOutputStream fileOutputStream = new FileOutputStream(outputFileName)
        ) {
            Buffer window = new Buffer();
            Buffer buffer = new Buffer();
            byte firstByte = randomAccessFile.readByte();
            fileOutputStream.write(firstByte);
            int step = 1;

            while (moveWindowAndBuffer(window, buffer, randomAccessFile, step)) {
                step = 1;
                Match match = checkMatches(window, buffer);
                if (match != null) {
                    byte[] str = ("<" + match.getOffset() + ";" + match.getLength() + ">").getBytes();
                    fileOutputStream.write(str);
                    step = match.getLength();
                } else {
                    if (buffer.getBuffer().size() == 0) {
                        return;
                    } else {
                        fileOutputStream.write(buffer.getBuffer().get(0));
                    }
                }
            }
            List<Byte> byteList = buffer.getBuffer();
            byteList.remove(0);
            for (Byte aByte : byteList) {
                fileOutputStream.write(aByte);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeBuffer(Buffer buffer, RandomAccessFile file) throws IOException {
        buffer.getBuffer().clear();
        file.seek(1);
        byte[] byteBuffer = new byte[BUFFER_SIZE];
        file.read(byteBuffer);
        for (byte b : byteBuffer) {
            buffer.getBuffer().add(b);
        }
        buffer.setStartBufferPosition(1);
    }

    private boolean moveWindowAndBuffer(Buffer window, Buffer buffer, RandomAccessFile file, int step) {
        try {
            file.seek(window.getStartBufferPosition() + window.getBuffer().size());

            byte[] byteBuffer = new byte[step];
            file.read(byteBuffer);

            for (byte b : byteBuffer) {
                window.getBuffer().add(b);
            }

            int bufferLength = window.getBuffer().size();
            if (bufferLength > MAX_WINDOW_SIZE) {
                int deletingCount = bufferLength - MAX_WINDOW_SIZE;
                for (int i = 0; i < deletingCount; i++) {
                    window.deleteCharAt(0);
                }
            }

            if (buffer.getBuffer().size() == 0) {
                initializeBuffer(buffer, file);
            } else {
                file.seek(buffer.getStartBufferPosition() + buffer.getBuffer().size() + step);
                if (file.read() == -1) {
                    if (buffer.getBuffer().size() > 1) {
                        for (int i = 0; i < step; i++) {
                            buffer.deleteCharAt(0);
                        }
                        return true;
                    } else {
                        throw new IOException("File end");
                    }
                }

                file.seek(buffer.getStartBufferPosition() + buffer.getBuffer().size());
                file.read(byteBuffer);
                for (byte b : byteBuffer) {
                    buffer.getBuffer().add(b);
                }
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
        List<Byte> byteMatches = new LinkedList<>();
        List<Byte> bytes = window.getBuffer();
        Match match = null;
        int windowOffset;
        int bufferOffset = 0;
        for (windowOffset = 0; windowOffset < bytes.size(); windowOffset++) {
            byte windowByte = bytes.get(windowOffset);
            try {
                if (windowByte == buffer.getBuffer().get(bufferOffset)) {
                    byteMatches.add(windowByte);
                    bufferOffset++;
                } else if (byteMatches.size() != 0) {
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        if (byteMatches.size() > 1) {
            match = new Match(
                    window.getStartBufferPosition() + windowOffset - byteMatches.size(),
                    byteMatches.size()
            );
        }
        return match;
    }

    public void decompress(String inputFileName, String outputFileName) {
        new File(outputFileName).delete();
        try (
                FileInputStream fileInputStream = new FileInputStream(inputFileName);
                RandomAccessFile randomAccessFile = new RandomAccessFile(outputFileName, "rw")
        ) {
            byte character;
            byte startByte = (byte) '<';
            while ((character = (byte) fileInputStream.read()) != -1) {
                if (character != startByte) {
                    randomAccessFile.write(character);
                } else {
                    Match match = readCodeBlock(fileInputStream);
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

    private Match readCodeBlock(FileInputStream fileInputStream) throws IOException {

        byte centerByte = (byte) ';';
        byte endByte = (byte) '>';
        byte b;
        List<Byte> byteList = new LinkedList<>();
        Match match = new Match();

        while ((b = (byte) fileInputStream.read()) != endByte) {
            if (b != centerByte) {
                byteList.add(b);
            } else {
                match.setOffset(Integer.valueOf(new String(byteListToByteArray(byteList))));
                byteList.clear();
            }
        }

        match.setLength(Integer.valueOf(new String(byteListToByteArray(byteList))));
       /*
        byte b = (byte) fileInputStream.read();
        match.setOffset(Character.getNumericValue((char) b));
        b = (byte) fileInputStream.read();
        b = (byte) fileInputStream.read();
        match.setLength(Character.getNumericValue((char) b));
        fileInputStream.read();*/
        return match;
    }
}
