package org.donntu.knt.mksit.lab2;

/**
 * @author Shilenko Alexander
 */
public class Match {
    private int offset;
    private int length;
    private byte nextByte;


    public Match() {
        this(-1,-1, (byte) -1);
    }

    public Match(int offset, int length, byte nextByte) {
        this.offset = offset;
        this.length = length;
        this.nextByte = nextByte;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int matchLength) {
        this.offset = matchLength;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int matDistance) {
        this.length = matDistance;
    }

    public byte getNextByte() {
        return nextByte;
    }

    public void setNextByte(byte nextByte) {
        this.nextByte = nextByte;
    }
}
