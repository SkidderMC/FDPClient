package org.cef.browser.scheme;

import org.cef.misc.IntRef;

/**
 * @author montoyo
 */
public class SchemeResponseData {

    private final byte[] data;
    private final int toRead;
    private final IntRef read;

    public SchemeResponseData(byte[] data, int toRead, IntRef read) {
        this.data = data;
        this.toRead = toRead;
        this.read = read;
    }

    public byte[] getDataArray() {
        return data;
    }

    public int getBytesToRead() {
        return toRead;
    }

    public void setAmountRead(int rd) {
        read.set(rd);
    }
}