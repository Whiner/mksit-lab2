package org.donntu.knt.mksit.lab2.v3;

public class FlyingWindow {
    private StringBuffer window = new StringBuffer();
    private int startWindowPosition = 0;

    private static int MAX_WINDOW_SIZE = 10;

    public static int getMaxWindowSize() {
        return MAX_WINDOW_SIZE;
    }

    public static void setMaxWindowSize(int maxWindowSize) {
        MAX_WINDOW_SIZE = maxWindowSize;
    }

    public StringBuffer getWindow() {
        return window;
    }

    public void setWindow(StringBuffer window) {
        this.window = window;
    }

    public int getStartWindowPosition() {
        return startWindowPosition;
    }

    public void setStartWindowPosition(int startWindowPosition) {
        this.startWindowPosition = startWindowPosition;
    }
}
