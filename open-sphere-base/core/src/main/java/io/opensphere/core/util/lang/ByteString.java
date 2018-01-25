package io.opensphere.core.util.lang;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.Utilities;

/**
 * A simple String like class that stores ASCII ( ISO-8858-1 ) characters as a
 * single byte per character. Should be less heavy weight than String.
 *
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class ByteString implements CharSequence, Serializable
{
    /** The serialVersionUID. */
    static final long serialVersionUID = 1L;

    /** The Constant ENCODING. */
    private static final String ENCODING = "ISO-8859-1";

    /** The Constant EMPTY. */
    public static final ByteString EMPTY = new ByteString("");

    /** The intern pool. */
    private static SharedObjectPool<ByteString> ourInternPool = new SharedObjectPool<>();

    /** The offset. */
    private final short myOffset;

    /** The end. */
    private final short myEnd;

    /** The data. */
    private final byte[] myData;

    /**
     * Gets the bytes for the provided sting with the standard encoding
     * ISO-8859-1.
     *
     * @param str the str
     * @return the bytes
     */
    public static byte[] getBytes(String str)
    {
        byte[] bytes = null;
        try
        {
            bytes = str.getBytes(ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ByteStringEncodingException("Unexpected: " + ENCODING + " not supported!", e);
        }
        return bytes;
    }

    /**
     * Gets the string from bytes using the standard ISO-8859-1 encoding.
     *
     * @param bytes the bytes
     * @return the string from bytes
     */
    public static String getStringFromBytes(byte[] bytes)
    {
        String retVal = null;
        try
        {
            retVal = new String(bytes, ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ByteStringEncodingException("Unexpected: " + ENCODING + " not supported!", e);
        }
        return retVal;
    }

    /**
     * Prints the number of keys in the intern pool.
     *
     * @return the string
     */
    public static String internStatus()
    {
        return new StringBuilder(64).append("Total Interned ByteString(s): ").append(ourInternPool.size()).toString();
    }

    /**
     * Instantiates a new byte string.
     *
     * @param data the data
     */
    public ByteString(byte[] data)
    {
        Utilities.checkNull(data, "data");
        myData = Arrays.copyOf(data, data.length);
        myOffset = 0;
        myEnd = (short)myData.length;
    }

    /**
     * Constructs a {@link ByteString} from a {@link String}.
     *
     * @param str The input string.
     */
    public ByteString(String str)
    {
        myData = ByteString.getBytes(str);
        myOffset = 0;
        myEnd = (short)myData.length;
    }

    /**
     * Constructs a ByteString from a data byte array, offset, and end point.
     *
     * @param data the data
     * @param offset the offset
     * @param end the end
     */
    private ByteString(byte[] data, int offset, int end)
    {
        myData = data;
        myOffset = (short)offset;
        myEnd = (short)end;
    }

    @Override
    public char charAt(int index)
    {
        int ix = index + myOffset;
        if (ix >= myEnd)
        {
            throw new StringIndexOutOfBoundsException("Invalid index " + index + " length " + length());
        }
        return (char)(myData[ix] & 0xff);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof ByteString)
        {
            ByteString s = (ByteString)obj;
            int i = myEnd - myOffset;
            if (i == s.myEnd - s.myOffset)
            {
                byte[] ac = myData;
                byte[] ac1 = s.myData;
                int j = myOffset;
                int k = s.myOffset;
                while (i-- != 0)
                {
                    if (ac[j++] != ac1[k++])
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a copy of the data backing this ByteString.
     *
     * @return the data
     */
    public byte[] getData()
    {
        return Arrays.copyOf(myData, myData.length);
    }

    @Override
    public int hashCode()
    {
        int i = 0;
        int j = myOffset;
        byte[] ac = myData;
        int k = myEnd - myOffset;
        for (int l = 0; l < k; l++)
        {
            i = 31 * i + ac[j++];
        }
        return i;
    }

    /**
     * Interns this string and returns the interned reference.
     *
     * @return the interned string reference
     */
    public ByteString intern()
    {
        return ourInternPool.get(this);
    }

    @Override
    public int length()
    {
        return myEnd - myOffset;
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
        if (start < 0 || end >= myEnd - myOffset)
        {
            throw new IllegalArgumentException("Illegal range " + start + "-" + end + " for sequence of length " + length());
        }
        return new ByteString(myData, start + myOffset, end + myOffset);
    }

    @Override
    public String toString()
    {
        try
        {
            return new String(myData, myOffset, myEnd - myOffset, ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ByteStringEncodingException("Unexpected: " + ENCODING + " not supported", e);
        }
    }

    /**
     * The Class ByteStringEncodingException.
     */
    public static class ByteStringEncodingException extends RuntimeException
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new byte string encoding exception.
         *
         * @param message the message
         * @param th the throwable cause
         */
        public ByteStringEncodingException(String message, Throwable th)
        {
            super(message, th);
        }
    }
}
