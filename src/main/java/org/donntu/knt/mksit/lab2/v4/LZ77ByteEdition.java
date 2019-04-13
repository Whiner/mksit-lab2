package org.donntu.knt.mksit.lab2.v4;

import org.donntu.knt.mksit.lab2.Match;

import java.io.*;
import java.util.Comparator;
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
            window.getBuffer().add(firstByte);
            fileOutputStream.write(firstByte);
            initializeBuffer(buffer, randomAccessFile);
            int step;
            do {
                step = 1;
                Match match = getMaxMatch(window, buffer);
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
            while (moveWindowAndBuffer(window, buffer, randomAccessFile, step));
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
            byte[] byteBuffer = new byte[step];
            file.seek(window.getStartBufferPosition() + window.length());
            file.read(byteBuffer);
            for (byte b : byteBuffer) {
                window.getBuffer().add(b);
            }
            if (window.length() > MAX_WINDOW_SIZE) {
                shortenBuffer(window, window.length() - MAX_WINDOW_SIZE);
            }

            int i = step + 1;
            do {
                i--;
                file.seek(buffer.getStartBufferPosition() + buffer.length() + i);
            } while(file.read() == -1);

            if(i == 0) {
                i++;
            }

            if (i < 0) {
                if (!shortenBuffer(buffer, step)) {
                    throw new IOException("File end");
                }
            } else {
                if (byteBuffer.length != i) {
                    byteBuffer = new byte[i];
                }

                file.seek(buffer.getStartBufferPosition() + buffer.length());
                file.read(byteBuffer);
                for (byte b : byteBuffer) {
                    buffer.getBuffer().add(b);
                }
                shortenBuffer(buffer, step);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean shortenBuffer(Buffer buffer, int count) {
        if (buffer.length() == 0) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            buffer.deleteCharAt(0);
        }
        return true;
    }

    private Match checkMatch(List<Byte> window, List<Byte> buffer) {
        List<Byte> byteMatches = new LinkedList<>();
        Match match = null;
        int windowOffset;
        int bufferOffset = 0;
        for (windowOffset = 0; windowOffset < window.size(); windowOffset++) {
            byte windowByte = window.get(windowOffset);
            try {
                if (windowByte == buffer.get(bufferOffset)) {
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
                    windowOffset - byteMatches.size(),
                    byteMatches.size()
            );
        }
        return match;
    }

    private Match getMaxMatch(Buffer window, Buffer buffer) {
        List<Byte> windowBytes = new LinkedList<>(window.getBuffer());
        int windowOffset = 0;
        List<Match> matches = new LinkedList<>();

        while (windowBytes.size() != 0) {
            Match match = checkMatch(windowBytes, buffer.getBuffer());
            if (match == null) {
                break;
            }
            match.setOffset(window.getStartBufferPosition() + windowOffset + match.getOffset());
            windowOffset = match.getOffset() + match.getLength();
            if (windowOffset > windowBytes.size()) {
                windowBytes.clear();
            } else {
                windowBytes = windowBytes.subList(windowOffset, windowBytes.size());
            }
            matches.add(match);
        }

        if (!matches.isEmpty()) {
            return matches.stream().max(Comparator.comparingInt(Match::getLength)).get();
        }
        return null;
    }

    public void decompress(String inputFileName, String outputFileName) {
        new File(outputFileName).delete();
        try (
                FileInputStream fileInputStream = new FileInputStream(inputFileName);
                RandomAccessFile randomAccessFile = new RandomAccessFile(outputFileName, "rw")
        ) {
            byte character;
            byte startByte = (byte) '<';
            int read;
            while ((read = fileInputStream.read()) != -1) {
                character = (byte) read;
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
        return match;
    }
}
