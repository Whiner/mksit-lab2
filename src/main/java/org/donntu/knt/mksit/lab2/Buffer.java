package org.donntu.knt.mksit.lab2;

import java.util.LinkedList;
import java.util.List;

public class Buffer {
    private List<Byte> buffer = new LinkedList<>();

    public List<Byte> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<Byte> buffer) {
        this.buffer = buffer;
    }


    public Buffer() {
    }

    public void deleteCharAt(int i) {
        buffer.remove(i);
    }

    public int length() {
        return buffer.size();
    }
}
