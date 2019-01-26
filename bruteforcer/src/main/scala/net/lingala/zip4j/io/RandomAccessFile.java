package net.lingala.zip4j.io;

import java.io.FileNotFoundException;
import java.io.IOException;

public class RandomAccessFile {
    public byte[] bytearray;
    public long pointer = 0;

    public RandomAccessFile(byte[] bytearray) throws FileNotFoundException {
        this.bytearray = bytearray.clone();
    }

    public long length() {
        return bytearray.length;
    }

    public void seek(long pos) throws IOException {
        if (pos < bytearray.length)
            pointer = pos;
        else throw new IOException("seek error");
    }

    public long getFilePointer() {
        return pointer;
    }

    public int read(byte[] b) throws IOException {
        return readBytes(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        readBytes(b, off, len);
    }

    public void close() throws IOException {
        pointer = 0;
    }

    public int readBytes(byte[] buffer, int offset, int length) throws IOException {
        if (pointer == bytearray.length) return -1;

        int i;
        for (i = offset; i < offset + length && pointer < bytearray.length; i++, pointer++) {
            if (i < buffer.length)
                buffer[i] = bytearray[(int)pointer];
            else throw new IOException("read error");
        }
        return i - offset;
    }
}
