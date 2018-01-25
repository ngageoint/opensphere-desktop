package io.opensphere.mantle.util.columnanalyzer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Single entry in the map for a given column key.
 */
@XmlRootElement(name = "ColumnAnalyzerXmlDataMapEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnAnalyzerXmlDataMapEntry
{
    /** The layer key. */
    @XmlAttribute(name = "key")
    private String myColumnKey;

    /** The list of SLDs for the layer. */
    @XmlElement(name = "ColumnAnalyzerData")
    private ColumnAnalyzerData myData;

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getColumnKey()
    {
        return myColumnKey;
    }

    /**
     * Gets the data.
     *
     * @return the list of data.
     */
    public ColumnAnalyzerData getData()
    {
        return myData;
    }

    /**
     * Sets the column key.
     *
     * @param key the new column key
     */
    public void setColumnKey(String key)
    {
        myColumnKey = key;
    }

    /**
     * Sets the {@link ColumnAnalyzerData}.
     *
     * @param data the ColumnAnalyzerData
     */
    public void setData(ColumnAnalyzerData data)
    {
        myData = data;
    }
}
