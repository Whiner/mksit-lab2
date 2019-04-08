package org.donntu.knt.mksit.lab2;

/**
 * @author Shilenko Alexander
 */
public class Match {
    private int offset;
    private int length;


    public Match() {
        this(-1,-1);
    }

    public Match(int offset, int length) {
        super();
        this.offset = offset;
        this.length = length;
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

}
