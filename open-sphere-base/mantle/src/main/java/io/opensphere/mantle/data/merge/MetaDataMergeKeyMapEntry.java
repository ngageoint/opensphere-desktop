package io.opensphere.mantle.data.merge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class MetaDataMergeKeyMapEntry.
 */
@XmlRootElement(name = "MetaDataMergeKeyMapEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaDataMergeKeyMapEntry
{
    /** The Merge key name. */
    @XmlAttribute(name = "mergeKeyName")
    private String myMergeKeyName;

    /** The Data type key name. */
    @XmlAttribute(name = "sourceKeyName")
    private String mySourceKeyName;

    /**
     * Instantiates a new meta data merge key map entry.
     */
    public MetaDataMergeKeyMapEntry()
    {
        /* intentionally blank */
    }

    /**
     * Copy constructor.
     *
     * @param other the MetaDataMergeKeyMapEntry to copy.
     */
    public MetaDataMergeKeyMapEntry(MetaDataMergeKeyMapEntry other)
    {
        myMergeKeyName = other.myMergeKeyName;
        mySourceKeyName = other.mySourceKeyName;
    }

    /**
     * Instantiates a new meta data merge key map entry.
     *
     * @param mergeKeyName the merge key name
     * @param dataTypeKeyName the data type key name
     */
    public MetaDataMergeKeyMapEntry(String mergeKeyName, String dataTypeKeyName)
    {
        myMergeKeyName = mergeKeyName;
        mySourceKeyName = dataTypeKeyName;
    }

    /**
     * Decode.
     *
     * @param ois the ObjectInputStream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void decode(ObjectInputStream ois) throws IOException
    {
        EncodeType et = EncodeType.decode(ois);
        myMergeKeyName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
        et = EncodeType.decode(ois);
        mySourceKeyName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
    }

    /**
     * Encode.
     *
     * @param oos the ObjectOutputStream
     * @return the number of bytes written.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public int encode(ObjectOutputStream oos) throws IOException
    {
        int numWritten = 0;

        numWritten += myMergeKeyName == null ? EncodeType.NULL.encode(oos) : DiskEncodeHelper.encodeString(oos, myMergeKeyName);

        numWritten += mySourceKeyName == null ? EncodeType.NULL.encode(oos) : DiskEncodeHelper.encodeString(oos, mySourceKeyName);

        return numWritten;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        MetaDataMergeKeyMapEntry other = (MetaDataMergeKeyMapEntry)obj;
        return EqualsHelper.equals(mySourceKeyName, other.mySourceKeyName)
                && EqualsHelper.equals(myMergeKeyName, other.myMergeKeyName);
    }

    /**
     * Gets the merge key name.
     *
     * @return the merge key name
     */
    public String getMergeKeyName()
    {
        return myMergeKeyName;
    }

    /**
     * Gets the source key name.
     *
     * @return the source key name
     */
    public String getSourceKeyName()
    {
        return mySourceKeyName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mySourceKeyName == null ? 0 : mySourceKeyName.hashCode());
        result = prime * result + (myMergeKeyName == null ? 0 : myMergeKeyName.hashCode());
        return result;
    }

    /**
     * Sets the merge key name.
     *
     * @param mergeKeyName the new merge key name
     */
    public void setMergeKeyName(String mergeKeyName)
    {
        myMergeKeyName = mergeKeyName;
    }

    /**
     * Sets the source key name.
     *
     * @param sourceKeyName the new source key name
     */
    public void setSourceKeyName(String sourceKeyName)
    {
        mySourceKeyName = sourceKeyName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getClass().getSimpleName()).append(" MergeKeyName[").append(myMergeKeyName).append("] SourceKeyName[")
                .append(mySourceKeyName).append(']');
        return sb.toString();
    }
}
