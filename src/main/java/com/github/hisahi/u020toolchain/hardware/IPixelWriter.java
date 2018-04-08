
package com.github.hisahi.u020toolchain.hardware;

public interface IPixelWriter {
    public void clear();
    public void write(int x, int y, int width, int height, int[] data, int stride);
}
