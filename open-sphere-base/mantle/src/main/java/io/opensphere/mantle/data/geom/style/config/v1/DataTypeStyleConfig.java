package io.opensphere.mantle.data.geom.style.config.v1;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class DataTypeStyleConfig.
 */
@XmlRootElement(name = "DataTypeStyle")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeStyleConfig
{
    /** The Data type key. */
    @XmlAttribute(name = "DataTypeKey")
    private String myDataTypeKey;

    /** The Feature type style config list. */
    @XmlElement(name = "FeatureTypeStyle")
    private List<FeatureTypeStyleConfig> myFeatureTypeStyleConfigList;

    /**
     * Instantiates a new data type style config.
     */
    public DataTypeStyleConfig()
    {
        myFeatureTypeStyleConfigList = New.list();
    }

    /**
     * Copy constructor.
     *
     * @param other the other
     */
    public DataTypeStyleConfig(DataTypeStyleConfig other)
    {
        Utilities.checkNull(other, "other");
        myDataTypeKey = other.getDataTypeKey();
        myFeatureTypeStyleConfigList = New.list();
        if (other.getDataTypeKey() != null)
        {
            other.myFeatureTypeStyleConfigList.stream().map(fts -> new FeatureTypeStyleConfig(fts))
                    .forEach(myFeatureTypeStyleConfigList::add);
        }
    }

    /**
     * Instantiates a new data type style config.
     *
     * @param dataTypeKey the data type key
     */
    public DataTypeStyleConfig(String dataTypeKey)
    {
        this();
        myDataTypeKey = dataTypeKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DataTypeStyleConfig other = (DataTypeStyleConfig)obj;
        if (myDataTypeKey == null)
        {
            if (other.myDataTypeKey != null)
            {
                return false;
            }
        }
        else if (!myDataTypeKey.equals(other.myDataTypeKey))
        {
            return false;
        }
        if (myFeatureTypeStyleConfigList == null)
        {
            if (other.myFeatureTypeStyleConfigList != null)
            {
                return false;
            }
        }
        else if (!myFeatureTypeStyleConfigList.equals(other.myFeatureTypeStyleConfigList))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets the data type key.
     *
     * @return the data type key
     */
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    /**
     * Gets the FeatureTypeStyleConfig for a specifc MGS base class name, or
     * null if not found.
     *
     * @param mgsBaseClassName the mgs base class name
     * @return the FeatureTypeStyleConfig for mgs base class or null if not
     *         found.
     */
    public FeatureTypeStyleConfig getFeatureTypeStyleConfigForMGSBaseClass(String mgsBaseClassName)
    {
        FeatureTypeStyleConfig cfg = null;
        if (myFeatureTypeStyleConfigList != null && !myFeatureTypeStyleConfigList.isEmpty())
        {
            cfg = myFeatureTypeStyleConfigList.stream()
                    .filter(ftsc -> Objects.equals(mgsBaseClassName, ftsc.getBaseMGSClassName())).findFirst().orElse(null);
        }
        return cfg;
    }

    /**
     * Gets the feature type style config list.
     *
     * @return the feature type style config list
     */
    public List<FeatureTypeStyleConfig> getFeatureTypeStyleConfigList()
    {
        return myFeatureTypeStyleConfigList;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDataTypeKey == null ? 0 : myDataTypeKey.hashCode());
        result = prime * result + (myFeatureTypeStyleConfigList == null ? 0 : myFeatureTypeStyleConfigList.hashCode());
        return result;
    }

    /**
     * Sets the data type key.
     *
     * @param dataTypeKey the new data type key
     */
    public void setDataTypeKey(String dataTypeKey)
    {
        myDataTypeKey = dataTypeKey;
    }

    /**
     * Sets the feature type style config list.
     *
     * @param featureTypeStyleConfigList the new feature type style config list
     */
    public void setFeatureTypeStyleConfigList(List<FeatureTypeStyleConfig> featureTypeStyleConfigList)
    {
        myFeatureTypeStyleConfigList = featureTypeStyleConfigList;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName()).append("\n" + "  DataTypeKey      [").append(myDataTypeKey)
                .append("]\n" + "  FeatureTypeStyleConfigList Size[").append(myFeatureTypeStyleConfigList.size()).append("]\n");

        if (!myFeatureTypeStyleConfigList.isEmpty())
        {
            sb.append('\n');
            for (FeatureTypeStyleConfig spc : myFeatureTypeStyleConfigList)
            {
                sb.append(spc.toString()).append("\n\n");
            }
        }
        return sb.toString();
    }
}
