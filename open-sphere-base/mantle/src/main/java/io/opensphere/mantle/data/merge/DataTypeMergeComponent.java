package io.opensphere.mantle.data.merge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class DataTypeMergeComponent.
 */
@XmlRootElement(name = "DataTypeMergeComponent")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeMergeComponent
{
    /** The Data type key. */
    @XmlAttribute(name = "sourceDataTypeDisplayName")
    private String myDataTypeDisplayName;

    /** The Data type key. */
    @XmlAttribute(name = "sourceDataTypeKey")
    private String myDTKey;

    /** The meta data merge key map entry list. */
    @XmlElement(name = "KeyMapEntry")
    private List<MetaDataMergeKeyMapEntry> myMetaDataMergeKeyMapEntryList;

    /** The list of all keys from the source. */
    @XmlElement(name = "SourceKey")
    private List<KeySpecification> mySourceKeyList;

    /**
     * Instantiates a new data type merge component.
     */
    public DataTypeMergeComponent()
    {
        myMetaDataMergeKeyMapEntryList = new ArrayList<>();
        mySourceKeyList = new ArrayList<>();
    }

    /**
     * Instantiates a new data type merge component.
     *
     * @param dti the {@link DataTypeInfo}
     */
    public DataTypeMergeComponent(DataTypeInfo dti)
    {
        this();
        myDTKey = dti.getTypeKey();
        myDataTypeDisplayName = dti.getDisplayName();
        if (dti.getMetaDataInfo() != null)
        {
            mySourceKeyList.addAll(KeySpecification.createKeySpecification(dti.getMetaDataInfo()));
        }
    }

    /**
     * Copy constructor.
     *
     * @param other the DataTypeMergeComponent to copy
     */
    public DataTypeMergeComponent(DataTypeMergeComponent other)
    {
        this();
        myDTKey = other.myDTKey;
        myDataTypeDisplayName = other.myDataTypeDisplayName;

        if (other.myMetaDataMergeKeyMapEntryList != null)
        {
            for (MetaDataMergeKeyMapEntry entry : other.myMetaDataMergeKeyMapEntryList)
            {
                myMetaDataMergeKeyMapEntryList.add(new MetaDataMergeKeyMapEntry(entry));
            }
        }

        if (other.mySourceKeyList != null)
        {
            for (KeySpecification key : other.mySourceKeyList)
            {
                mySourceKeyList.add(new KeySpecification(key));
            }
        }
    }

    /**
     * Instantiates a new data type merge component.
     *
     * @param dtiKey the DataTypeInfo key
     * @param dtiDispName the dti disp name
     */
    public DataTypeMergeComponent(String dtiKey, String dtiDispName)
    {
        this();
        myDTKey = dtiKey;
        myDataTypeDisplayName = dtiDispName;
    }

    /**
     * Adds the meta data merge key map entry to the entry list.
     *
     * @param entry the entry
     * @return true, if successful
     */
    public boolean addMetaDataMergeKeyMapEntry(MetaDataMergeKeyMapEntry entry)
    {
        return myMetaDataMergeKeyMapEntryList.add(entry);
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
        myDataTypeDisplayName = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;
        et = EncodeType.decode(ois);
        myDTKey = et == EncodeType.STRING ? DiskDecodeHelper.decodeString(ois) : null;

        et = EncodeType.decode(ois);
        if (et == EncodeType.NULL)
        {
            mySourceKeyList = null;
        }
        else if (et == EncodeType.LIST)
        {
            int numEntries = ois.readShort();
            for (int i = 0; i < numEntries; i++)
            {
                et = EncodeType.decode(ois);
                if (et == EncodeType.NULL)
                {
                    mySourceKeyList.add(null);
                }
                else
                {
                    KeySpecification ks = new KeySpecification();
                    ks.decode(ois);
                    mySourceKeyList.add(ks);
                }
            }
        }

        et = EncodeType.decode(ois);
        if (et == EncodeType.NULL)
        {
            myMetaDataMergeKeyMapEntryList = null;
        }
        else if (et == EncodeType.LIST)
        {
            int numEntries = ois.readShort();
            for (int i = 0; i < numEntries; i++)
            {
                et = EncodeType.decode(ois);
                if (et == EncodeType.NULL)
                {
                    myMetaDataMergeKeyMapEntryList.add(null);
                }
                else
                {
                    MetaDataMergeKeyMapEntry entry = new MetaDataMergeKeyMapEntry();
                    entry.decode(ois);
                    myMetaDataMergeKeyMapEntryList.add(entry);
                }
            }
        }
    }

    /**
     * Encode.
     *
     * @param oos the ObjectOutputStream
     * @return the number of bytes written.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public int encode(ObjectOutputStream oos) throws IOException
    {
        int numWritten = 0;

        numWritten += myDataTypeDisplayName == null ? EncodeType.NULL.encode(oos)
                : DiskEncodeHelper.encodeString(oos, myDataTypeDisplayName);

        numWritten += myDTKey == null ? EncodeType.NULL.encode(oos) : DiskEncodeHelper.encodeString(oos, myDTKey);

        if (mySourceKeyList == null)
        {
            numWritten += EncodeType.NULL.encode(oos);
        }
        else
        {
            numWritten += EncodeType.LIST.encode(oos);
            oos.writeShort((short)mySourceKeyList.size());
            numWritten += 2;
            for (KeySpecification ks : mySourceKeyList)
            {
                if (ks == null)
                {
                    numWritten += EncodeType.NULL.encode(oos);
                }
                else
                {
                    numWritten += EncodeType.LIST_ENTRY.encode(oos);
                    numWritten += ks.encode(oos);
                }
            }
        }

        if (myMetaDataMergeKeyMapEntryList == null)
        {
            numWritten += EncodeType.NULL.encode(oos);
        }
        else
        {
            numWritten += EncodeType.LIST.encode(oos);
            oos.writeShort((short)myMetaDataMergeKeyMapEntryList.size());
            numWritten += 2;
            for (MetaDataMergeKeyMapEntry entry : myMetaDataMergeKeyMapEntryList)
            {
                if (entry == null)
                {
                    numWritten += EncodeType.NULL.encode(oos);
                }
                else
                {
                    numWritten += EncodeType.LIST_ENTRY.encode(oos);
                    numWritten += entry.encode(oos);
                }
            }
        }

        return numWritten;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!Utilities.sameInstance(this, obj))
        {
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            DataTypeMergeComponent other = (DataTypeMergeComponent)obj;
            if (!Objects.equals(myDataTypeDisplayName, other.myDataTypeDisplayName) || !Objects.equals(myDTKey, other.myDTKey)
                    || !Objects.equals(myMetaDataMergeKeyMapEntryList, other.myMetaDataMergeKeyMapEntryList)
                    || !Objects.equals(mySourceKeyList, other.mySourceKeyList))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the data type display name.
     *
     * @return the data type display name
     */
    public String getDataTypeDisplayName()
    {
        return myDataTypeDisplayName;
    }

    /**
     * Gets the data type key for the merge.
     *
     * @return the data type key
     */
    public String getDataTypeKey()
    {
        return myDTKey;
    }

    /**
     * Gets the meta data merge key map entry list.
     *
     * @return the meta data merge key map entry list
     */
    public List<MetaDataMergeKeyMapEntry> getMetaDataMergeKeyMapEntryList()
    {
        return myMetaDataMergeKeyMapEntryList;
    }

    /**
     * Gets the source key list.
     *
     * @return the source key list
     */
    public List<KeySpecification> getSourceKeyList()
    {
        return mySourceKeyList;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDataTypeDisplayName == null ? 0 : myDataTypeDisplayName.hashCode());
        result = prime * result + (myDTKey == null ? 0 : myDTKey.hashCode());
        result = prime * result + (myMetaDataMergeKeyMapEntryList == null ? 0 : myMetaDataMergeKeyMapEntryList.hashCode());
        result = prime * result + (mySourceKeyList == null ? 0 : mySourceKeyList.hashCode());
        return result;
    }

    /**
     * Sets the data type display name.
     *
     * @param dataTypeDisplayName the new data type display name
     */
    public void setDataTypeDisplayName(String dataTypeDisplayName)
    {
        myDataTypeDisplayName = dataTypeDisplayName;
    }

    /**
     * Sets the data type key for the merge.
     *
     * @param dataTypeKey the new data type key
     */
    public void setDataTypeKey(String dataTypeKey)
    {
        myDTKey = dataTypeKey;
    }

    /**
     * Sets the meta data merge key map entry list.
     *
     * @param entryList the new meta data merge key map entry list
     */
    public void setMetaDataMergeKeyMapEntryList(List<MetaDataMergeKeyMapEntry> entryList)
    {
        myMetaDataMergeKeyMapEntryList = entryList == null ? new ArrayList<>() : entryList;
    }

    /**
     * Sets the source key list.
     *
     * @param sourceKeyList the new source key list
     */
    public void setSourceKeyList(List<KeySpecification> sourceKeyList)
    {
        mySourceKeyList = sourceKeyList;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getName());
        sb.append(" DataTypeDisplayName[").append(myDataTypeDisplayName);
        sb.append("] DataTypeKey[").append(myDTKey);
        sb.append("]\n SourceKey List[").append(mySourceKeyList == null ? null : Integer.valueOf(mySourceKeyList.size()))
                .append("]\n");
        if (mySourceKeyList != null)
        {
            for (KeySpecification ks : mySourceKeyList)
            {
                sb.append("   ").append(ks.toString()).append('\n');
            }
        }
        sb.append(" KeyMapEntry List[")
                .append(myMetaDataMergeKeyMapEntryList == null ? null : Integer.valueOf(myMetaDataMergeKeyMapEntryList.size()))
                .append("]\n");
        if (myMetaDataMergeKeyMapEntryList != null)
        {
            for (MetaDataMergeKeyMapEntry entry : myMetaDataMergeKeyMapEntryList)
            {
                sb.append("   ").append(entry.toString()).append('\n');
            }
        }
        return sb.toString();
    }
}
