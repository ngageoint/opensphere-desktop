package io.opensphere.core.common.transfer.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A POJO in which a key and a value are stored. Annotated for XML output.
 */
public class MapEntryType
{

    /**
     * The name of the entry.
     */
    @XmlAttribute
    public String key;

    /**
     * The value of the entry.
     */
    @XmlValue
    public String value;

}
