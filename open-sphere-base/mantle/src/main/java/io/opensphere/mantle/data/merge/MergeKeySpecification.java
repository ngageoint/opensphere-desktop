package io.opensphere.mantle.data.merge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class MergeKeySpecification.
 */
@XmlRootElement(name = "MergeKeySpecification")
@XmlAccessorType(XmlAccessType.FIELD)
public class MergeKeySpecification extends KeySpecification
{
    /** The Conversion hint. */
    @XmlAttribute(name = "conversionHint")
    private ConversionHint myConversionHint = ConversionHint.NONE;

    /**
     * Instantiates a new merge key specification.
     */
    public MergeKeySpecification()
    {
        super();
    }

    /**
     * Copy constructor (Deep copy).
     *
     * @param other the MergeKeySpecification to copy.
     */
    public MergeKeySpecification(MergeKeySpecification other)
    {
        super(other);
        myConversionHint = other.myConversionHint;
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     */
    public MergeKeySpecification(String keyName, String className)
    {
        super(keyName, className, null);
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     * @param ch the ch
     */
    public MergeKeySpecification(String keyName, String className, ConversionHint ch)
    {
        this(keyName, className, null, ch);
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     * @param skClassName the sk type
     */
    public MergeKeySpecification(String keyName, String className, String skClassName)
    {
        super(keyName, className, skClassName);
    }

    /**
     * Instantiates a new merge key specification.
     *
     * @param keyName the key name
     * @param className the class name
     * @param skClassName the sk type
     * @param ch the ch
     */
    public MergeKeySpecification(String keyName, String className, String skClassName, ConversionHint ch)
    {
        super(keyName, className, skClassName);
        myConversionHint = ch;
    }

    @Override
    public void decode(ObjectInputStream ois) throws IOException
    {
        super.decode(ois);
        EncodeType et = EncodeType.decode(ois);
        String conversionHintStr = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
        myConversionHint = et == EncodeType.STRING ? ConversionHint.valueOf(conversionHintStr) : null;
    }

    @Override
    public int encode(ObjectOutputStream oos) throws IOException
    {
        int numWritten = super.encode(oos);

        numWritten += myConversionHint == null ? EncodeType.NULL.encode(oos)
                : DiskEncodeHelper.encodeString(oos, myConversionHint.toString());

        return numWritten;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        MergeKeySpecification other = (MergeKeySpecification)obj;
        return Objects.equals(myConversionHint, other.myConversionHint);
    }

    /**
     * Gets the conversion hint.
     *
     * @return the conversion hint
     */
    public ConversionHint getConversionHint()
    {
        return myConversionHint;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myConversionHint == null ? 0 : myConversionHint.hashCode());
        return result;
    }

    /**
     * Sets the conversion hint.
     *
     * @param conversionHint the new conversion hint
     */
    public void setConversionHint(ConversionHint conversionHint)
    {
        myConversionHint = conversionHint;
        if (myConversionHint == null)
        {
            myConversionHint = ConversionHint.NONE;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(super.toString());
        sb.append(" ConversionHint[").append(myConversionHint).append(']');
        return sb.toString();
    }

    /**
     * The Enum ConversionHint.
     */
    public enum ConversionHint
    {
        /** CONVERT_TO_DOUBLE. */
        CONVERT_TO_DOUBLE,

        /** CONVERT_TO_INTEGER. */
        CONVERT_TO_INTEGER,

        /** CONVERT_TO_LONG. */
        CONVERT_TO_LONG,

        /** CONVERT_TO_SHORT. */
        CONVERT_TO_SHORT,

        /** CONVERT_TO_STRING. */
        CONVERT_TO_STRING,

        /** NONE. */
        NONE
    }
}
