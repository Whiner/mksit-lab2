package org.donntu.knt.mksit.lab2;

public class Buffer {
    private StringBuffer buffer = new StringBuffer();
    private int startBufferPosition = 0;

    private static int MAX_WINDOW_SIZE = 10;

    public static int getMaxWindowSize() {
        return MAX_WINDOW_SIZE;
    }

    public static void setMaxWindowSize(int maxWindowSize) {
        MAX_WINDOW_SIZE = maxWindowSize;
    }

    public StringBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuffer buffer) {
        this.buffer = buffer;
    }

    public int getStartBufferPosition() {
        return startBufferPosition;
    }

    public void setStartBufferPosition(int startBufferPosition) {
        this.startBufferPosition = startBufferPosition;
    }

    public Buffer(StringBuffer buffer, int startBufferPosition) {
        this.buffer = buffer;
        this.startBufferPosition = startBufferPosition;
    }

    public Buffer() {
    }

    public void deleteCharAt(int i) {
        buffer.deleteCharAt(i);
        startBufferPosition++;
    }
}
