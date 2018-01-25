package io.opensphere.mantle.util.columnanalyzer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Single entry in the map for a given column key.
 */
@XmlRootElement(name = "DataTypeAnalaysMapXmMapEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeAnalaysMapXmMapEntry
{
    /** The list of SLDs for the layer. */
    @XmlElement(name = "DataTypeColumnAnalyzerDataSet")
    private DataTypeColumnAnalyzerDataSet myData;

    /** The layer key. */
    @XmlAttribute(name = "key")
    private String myTypeKey;

    /**
     * Gets the data.
     *
     * @return the list of data.
     */
    public DataTypeColumnAnalyzerDataSet getData()
    {
        return myData;
    }

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /**
     * Sets the {@link ColumnAnalyzerData}.
     *
     * @param data the ColumnAnalyzerData
     */
    public void setData(DataTypeColumnAnalyzerDataSet data)
    {
        myData = data;
    }

    /**
     * Sets the type key.
     *
     * @param key the new column key
     */
    public void setTypeKey(String key)
    {
        myTypeKey = key;
    }
}
