package org.donntu.knt.mksit.lab2.v3;

/**
 * @author Shilenko Alexander
 */
public class Match {
    private int offset;
    private int length;


    public Match() {
        this(-1,-1);
    }

    public Match(int matchLength, int matDistance) {
        super();
        this.offset = matchLength;
        this.length = matDistance;
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
