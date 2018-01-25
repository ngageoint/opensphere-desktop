package io.opensphere.core.common.util;

import java.io.DataInputStream;
import java.io.IOException;

public class StreamUtil
{

    /**
     * Read 32bits from a stream and get it into a float format.
     *
     * @param in
     * @return
     */
    public static float getFloatFrom32Bit(DataInputStream in) throws IOException
    {

        int bits = 0;
        byte[] bytes = new byte[4];
        in.read(bytes);

        // bytes are signed, so make sure you AND them with 0xff if it is not
        // the leading byte.
        bits = ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
        float retval = Float.intBitsToFloat(bits);
        return retval;
    }

    /**
     * Read 64bits from a stream and get it into a long format.
     *
     * @param in
     * @return
     */
    public static long getLongFrom64Bit(DataInputStream in) throws IOException
    {

        long bits = 0;
        byte[] bytes = new byte[8];
        in.read(bytes);
        // bytes are signed, so make sure you AND them with 0xff if it is not
        // the leading byte.
        bits = ((bytes[0] & 0xffL) << 56) | ((bytes[1] & 0xffL) << 48) | ((bytes[2] & 0xffL) << 40) | ((bytes[3] & 0xffL) << 32)
                | ((bytes[4] & 0xffL) << 24) | ((bytes[5] & 0xffL) << 16) | ((bytes[6] & 0xffL) << 8) | (bytes[7] & 0xffL);
        return bits;
    }

}
