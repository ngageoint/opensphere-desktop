package io.opensphere.mantle.icon.config.v1;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * The Class IconRegistryConfig.
 */
@XmlRootElement(name = "IconRegistryConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class IconRegistryConfig
{
    /** The Icon records. */
    @XmlElement(name = "IconRecord")
    private final List<IconRecordConfig> myIconRecords;

    /**
     * Instantiates a new icon registry config.
     */
    public IconRegistryConfig()
    {
        myIconRecords = New.list();
    }

    /**
     * Gets the icon records.
     *
     * @return the icon records
     */
    public List<IconRecordConfig> getIconRecords()
    {
        return myIconRecords;
    }
}
