package io.opensphere.mantle.data.merge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class DataTypeMergeMap.
 */
@XmlRootElement(name = "DataTypeMergeMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeMergeMap
{
    /** The Merge components. */
    @XmlElement(name = "DataTypeMergeComponent")
    private List<DataTypeMergeComponent> myMergeComponents;

    /** The Merged key names. */
    @XmlElement(name = "MergedKeyName")
    private List<MergeKeySpecification> myMergedKeyNames;

    /**
     * Creates the merge map for single source.
     *
     * @param dti the dti
     * @return the data type merge map
     */
    public static DataTypeMergeMap createMergeMapForSingleSource(DataTypeInfo dti)
    {
        Utilities.checkNull(dti, "dti");
        DataTypeMergeMap map = new DataTypeMergeMap();
        MetaDataInfo mdi = dti.getMetaDataInfo();
        if (mdi != null)
        {
            DataTypeMergeComponent comp = new DataTypeMergeComponent(dti);
            for (String key : mdi.getKeyNames())
            {
                SpecialKey sk = mdi.getSpecialTypeForKey(key);
                Class<?> cl = mdi.getKeyClassType(key);
                map.getMergedKeyNames()
                        .add(new MergeKeySpecification(key, cl.getName(), sk == null ? null : sk.getClass().getName()));
                comp.addMetaDataMergeKeyMapEntry(new MetaDataMergeKeyMapEntry(key, key));
            }
            map.getMergeComponents().add(comp);
        }
        return map;
    }

    /**
     * Instantiates a new data type merge map.
     */
    public DataTypeMergeMap()
    {
        myMergedKeyNames = new ArrayList<>();
        myMergeComponents = new ArrayList<>();
    }

    /**
     * Copy constructor. (Deep copy)
     *
     * @param other the other to copy.
     */
    public DataTypeMergeMap(DataTypeMergeMap other)
    {
        this();
        if (other.myMergedKeyNames != null)
        {
            for (MergeKeySpecification spec : other.myMergedKeyNames)
            {
                myMergedKeyNames.add(new MergeKeySpecification(spec));
            }
        }

        if (other.myMergeComponents != null)
        {
            for (DataTypeMergeComponent comp : other.myMergeComponents)
            {
                myMergeComponents.add(new DataTypeMergeComponent(comp));
            }
        }
    }

    /**
     * Decode.
     *
     * @param ois the ois
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void decode(ObjectInputStream ois) throws IOException
    {
        EncodeType et = EncodeType.decode(ois);
        if (et == EncodeType.NULL)
        {
            myMergedKeyNames = null;
        }
        else if (et == EncodeType.LIST)
        {
            int numEntries = ois.readShort();
            for (int i = 0; i < numEntries; i++)
            {
                et = EncodeType.decode(ois);
                if (et == EncodeType.NULL)
                {
                    myMergedKeyNames.add(null);
                }
                else
                {
                    MergeKeySpecification mks = new MergeKeySpecification();
                    mks.decode(ois);
                    myMergedKeyNames.add(mks);
                }
            }

            et = EncodeType.decode(ois);
            if (et == EncodeType.NULL)
            {
                myMergeComponents = null;
            }
            else if (et == EncodeType.LIST)
            {
                numEntries = ois.readShort();
                for (int i = 0; i < numEntries; i++)
                {
                    et = EncodeType.decode(ois);
                    if (et == EncodeType.NULL)
                    {
                        myMergeComponents.add(null);
                    }
                    else
                    {
                        DataTypeMergeComponent comp = new DataTypeMergeComponent();
                        comp.decode(ois);
                        myMergeComponents.add(comp);
                    }
                }
            }
        }
    }

    /**
     * Encode.
     *
     * @param oos the oos
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public int encode(ObjectOutputStream oos) throws IOException
    {
        int numWritten = 0;
        if (myMergedKeyNames == null)
        {
            numWritten += EncodeType.NULL.encode(oos);
        }
        else
        {
            numWritten += EncodeType.LIST.encode(oos);
            oos.writeShort((short)myMergedKeyNames.size());
            numWritten += 2;
            for (MergeKeySpecification mks : myMergedKeyNames)
            {
                if (mks == null)
                {
                    numWritten += EncodeType.NULL.encode(oos);
                }
                else
                {
                    numWritten += EncodeType.LIST_ENTRY.encode(oos);
                    numWritten += mks.encode(oos);
                }
            }
        }

        if (myMergeComponents == null)
        {
            numWritten += EncodeType.NULL.encode(oos);
        }
        else
        {
            numWritten += EncodeType.LIST.encode(oos);
            oos.writeShort((short)myMergeComponents.size());
            numWritten += 2;
            for (DataTypeMergeComponent mks : myMergeComponents)
            {
                if (mks == null)
                {
                    numWritten += EncodeType.NULL.encode(oos);
                }
                else
                {
                    numWritten += EncodeType.LIST_ENTRY.encode(oos);
                    numWritten += mks.encode(oos);
                }
            }
        }

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
        DataTypeMergeMap other = (DataTypeMergeMap)obj;
        if (!Objects.equals(myMergeComponents, other.myMergeComponents))
        {
            return false;
        }
        return Objects.equals(myMergedKeyNames, other.myMergedKeyNames);
    }

    /**
     * Gets the contributing merge data type display names.
     *
     * @return the contributing merge data type display names
     */
    public List<String> getContributingMergeDataTypeDisplayNames()
    {
        List<String> result = new ArrayList<>();
        for (DataTypeMergeComponent comp : myMergeComponents)
        {
            result.add(comp.getDataTypeDisplayName());
        }
        return result;
    }

    /**
     * Gets the contributing merge data type keys.
     *
     * @return the contributing merge data type keys
     */
    public List<String> getContributingMergeDataTypeKeys()
    {
        List<String> result = new ArrayList<>();
        for (DataTypeMergeComponent comp : myMergeComponents)
        {
            result.add(comp.getDataTypeKey());
        }
        return result;
    }

    /**
     * Gets the merge components.
     *
     * @return the merge components
     */
    public List<DataTypeMergeComponent> getMergeComponents()
    {
        return myMergeComponents;
    }

    /**
     * Gets the merged key names.
     *
     * @return the merged key names
     */
    public List<MergeKeySpecification> getMergedKeyNames()
    {
        return myMergedKeyNames;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myMergeComponents == null ? 0 : myMergeComponents.hashCode());
        result = prime * result + (myMergedKeyNames == null ? 0 : myMergedKeyNames.hashCode());
        return result;
    }

    /**
     * Checks for for a merged key with the special type.
     *
     * @param keyType the keyType class
     * @return true, if successful
     */
    public boolean hasSpecialKeyType(Class<? extends SpecialKey> keyType)
    {
        Utilities.checkNull(keyType, "keyType");
        boolean found = false;
        if (myMergedKeyNames != null)
        {
            for (MergeKeySpecification spec : myMergedKeyNames)
            {
                if (keyType.getName().equals(spec.getSpecialKeyClassName()))
                {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append("\n" + "MergeKeyName List[")
                .append(myMergedKeyNames == null ? "NULL" : myMergedKeyNames.size()).append("] \n");
        if (myMergedKeyNames != null)
        {
            for (MergeKeySpecification mks : myMergedKeyNames)
            {
                sb.append("   ").append(mks.toString()).append('\n');
            }
        }
        sb.append("DataTypeMergeComponent List[").append(myMergeComponents == null ? "NULL" : myMergeComponents.size())
                .append("] \n");
        if (myMergeComponents != null)
        {
            for (DataTypeMergeComponent mks : myMergeComponents)
            {
                sb.append("   ").append(mks.toString()).append('\n');
            }
        }
        return sb.toString();
    }

    // /**
    // * Loads a DataTypeMergeMap from an xml file.
    // *
    // * @param aFileToLoad the file to load
    // * @return the DataTypeMergeMap or null if not found or a problem occurs
    // * while loading.
    // */
    // public static DataTypeMergeMap load(File aFileToLoad)
    // {
    // DataTypeMergeMap map = null;
    // try
    // {
    //
    // if (aFileToLoad.exists())
    // {
    // map = XMLUtilities.readXMLObject(aFileToLoad, DataTypeMergeMap.class);
    // }
    // else
    // {
    // LOGGER.info("DataTypeMergeMap file not found: " + aFileToLoad.getPath());
    // map = null;
    // }
    // }
    // catch (JAXBException e)
    // {
    // LOGGER.warn("Error loading DataTypeMergeMap file " +
    // aFileToLoad.getPath() + " ", e);
    // map = null;
    // }
    // return map;
    // }
    //
    // /**
    // * Saves the {@link DataTypeMergeMap} to the specified file.
    // *
    // * @param map the map to save
    // * @param aFileToSaveTo the a file to save to
    // * @return true, if successful
    // */
    // public static boolean save(DataTypeMergeMap map, File aFileToSaveTo)
    // {
    // boolean success = true;
    // try
    // {
    // LOGGER.info("Saving DataTypeMergeMap to " + aFileToSaveTo);
    // XMLUtilities.writeXMLObject(map, aFileToSaveTo);
    // }
    // catch (JAXBException exc)
    // {
    // LOGGER.warn("Error saving DataTypeMergeMap file", exc);
    // success = false;
    // }
    // return success;
    // }
}
