package io.opensphere.core.common.shapefile.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import io.opensphere.core.common.shapefile.v2.dbase.DbfFieldType;

/**
 * <pre>
 *   Byte Position Field           Units   Type              Order
 *   ==============================================================
 *   Byte 0        Name            Ascii   Byte[11]          n/a
 *   Byte 11       Type            Ascii   (C,D,L,M, or N)   n/a
 *   Byte 12       Address         hex     Int               n/a
 *   Byte 16       Field Length    bytes   Byte              n/a
 *   Byte 17       Decimal Count   Count   Byte              n/a
 *   Byte 18       reserved                Byte[2]           n/a
 *   Byte 20       Work area ID            Byte              n/a
 *   Byte 21       reserved                Byte[2]           n/a
 *   Byte 23       Set Fields      Flag    Byte              n/a
 *   Byte 24       reserved                Byte[7]           n/a
 *   Byte 31       Index Field     Flag    Byte              n/a
 * </pre>
 */
public class DBFColumnInfo
{

    // Not sure why I'm duplicating this. Made sense at the time!
    public String fieldName = null;

    public char type;

    // unsigned 0-255

    public short length;

    private short decimalCount = 0;

    public DBFColumnInfo()
    {
    }

    /**
     * Constructor.
     *
     * @param name the column name up to 11 characters long.
     * @param type the column type: C, D, L, M or N.
     * @param length the field length (0-255).
     */
    public DBFColumnInfo(String name, char type, short length)
    {
        fieldName = name;
        this.type = type;
        this.length = length;
    }

    /**
     * Constructor.
     *
     * @param name the column name up to 11 characters long.
     * @param type the column type: C, D, L, M or N.
     * @param length the field length (0-255).
     * @param decimalCount the field length (0-255).
     */
    public DBFColumnInfo(String name, char type, short length, short decimalCount)
    {
        this(name, type, length);
        this.decimalCount = decimalCount;
    }

    public boolean parseFieldDefinition(ByteBuffer buffer)
    {
        boolean returnValue = true;

        byte[] charBuffer = new byte[11];
        buffer.get(charBuffer);
        /* hope this conversion works */
        type = (char)buffer.get();
        int nameLength = 0;
        for (byte b : charBuffer)
        {
            if (b == '\0')
            {
                break;
            }
            ++nameLength;
        }
        fieldName = new String(charBuffer, 0, nameLength);
        @SuppressWarnings("unused")
        int filler = buffer.getInt();
        length = (short)(0xFF & buffer.get());

        byte[] unusedBuffer = new byte[15];
        buffer.get(unusedBuffer);

        return returnValue;
    }

    public boolean writeFieldDefinition(ByteBuffer buffer)
    {
        boolean returnValue = true;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int count = 0;
        while (count < 11)
        {
            if (count < fieldName.length())
            {
                buffer.put((byte)fieldName.charAt(count));
            }
            else
            {
                buffer.put((byte)'\0');
            }
            count++;
        }

        // To do, get this into the enum above

        // byte 11
        // Should work for our characters, maybe
        buffer.put((byte)type);
        // byte 12-15
        buffer.putInt(0);
        // byte 16 length
        buffer.put((byte)(0xFF & length));
        // byte 17 decimal count
        buffer.put((byte)(0xFF & decimalCount));
        buffer.putShort((short)0);
        buffer.put((byte)0);
        buffer.putShort((short)0);
        buffer.put((byte)0);
        buffer.put(new byte[7]);
        buffer.put((byte)0);
        return returnValue;
    }

    public Object parseFieldRecord(ByteBuffer buffer, boolean convertToActualIfPossible)
    {
        Object result = null;
        byte[] charBuffer = new byte[length];
        buffer.get(charBuffer);
        if (convertToActualIfPossible)
        {
            try
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(charBuffer);
                DataInputStream dis = new DataInputStream(bais);
                if (type == 'I')
                {
                    int i = dis.read();
                    result = Integer.valueOf(i);
                }
                else if (type == 'O')
                {
                    double d = dis.readDouble();
                    result = Double.valueOf(d);
                }
            }
            catch (IOException e)
            {
                // Ground this exception.
            }
        }
        return result == null ? new String(charBuffer) : result;
    }

    public void writeFieldRecord(Object record, ByteBuffer buffer)
    {
        try
        {
            if (record == null)
            {
                record = new String("");
            }

            byte[] recordArray = record.toString().getBytes("US-ASCII");
            // The use of toString here can be sketchy, but it should
            // give the desired behavior for all data types that we're
            // worried about.

            byte[] filledRecord = Arrays.copyOf(recordArray, length);
            buffer.put(filledRecord);
        }
        catch (UnsupportedEncodingException e)
        {
            // Shouldn't ever get here.
            e.printStackTrace();
        }
    }

    public DbfFieldType getType()
    {
        return DbfFieldType.getInstance(type);
    }

    /**
     * Gets the value of the {@link #fieldName} field.
     *
     * @return the value stored in the {@link #fieldName} field.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "DBFColumnInfo [fieldName=" + fieldName + ", length=" + length + ", type=" + type + "]";
    }

}
