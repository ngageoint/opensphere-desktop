package io.opensphere.wps.config.v2;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.common.transfer.bean.MapAdapter;

/** A process setting entry. */
@XmlRootElement(name = "process")
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessSetting
{
    /** The identifier. */
    @XmlAttribute(name = "identifier")
    private String myIdentifier;

    /** The map of field to last used value. */
    @XmlElement(name = "lastUsedValue")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private final Map<String, String> myLastUsedValues = new HashMap<>();

    /** The map of layer to last used column. */
    @XmlElement(name = "lastUsedColumn")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private final Map<String, String> myLastUsedColumns = new HashMap<>();

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    public String getIdentifier()
    {
        return myIdentifier;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the identifier
     */
    public void setIdentifier(String identifier)
    {
        myIdentifier = identifier;
    }

    /**
     * Gets the map of field to last used value.
     *
     * @return the map of field to last used value
     */
    public Map<String, String> getLastUsedValues()
    {
        return myLastUsedValues;
    }

    /**
     * Gets the map of layer to last used column.
     *
     * @return the map of layer to last used column
     */
    public Map<String, String> getLastUsedColumns()
    {
        return myLastUsedColumns;
    }
}
