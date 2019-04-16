package org.donntu.knt.mksit.lab2;

import java.util.LinkedList;
import java.util.List;

public class Buffer {
    private List<Byte> buffer = new LinkedList<>();
    private int startBufferPosition = 0;

    private static int MAX_WINDOW_SIZE = 10;

    public static int getMaxWindowSize() {
        return MAX_WINDOW_SIZE;
    }

    public static void setMaxWindowSize(int maxWindowSize) {
        MAX_WINDOW_SIZE = maxWindowSize;
    }

    public List<Byte> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<Byte> buffer) {
        this.buffer = buffer;
    }

    public int getStartBufferPosition() {
        return startBufferPosition;
    }

    public void setStartBufferPosition(int startBufferPosition) {
        this.startBufferPosition = startBufferPosition;
    }

    public Buffer(List<Byte> buffer, int startBufferPosition) {
        this.buffer = buffer;
        this.startBufferPosition = startBufferPosition;
    }

    public Buffer() {
    }

    public void deleteCharAt(int i) {
        buffer.remove(i);
        startBufferPosition++;
    }

    public int length() {
        return buffer.size();
    }
}
