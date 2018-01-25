package io.opensphere.wms.sld.config.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.Toolbox;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * The Class SldConfiguration.
 */
@XmlRootElement(name = "SldConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SldConfiguration
{
    /** Map of layers to their associated list of SLDs. */
    @XmlElement(name = "LayerMap")
    @XmlJavaTypeAdapter(SldXmlAdapter.class)
    private Map<String, List<StyledLayerDescriptor>> myLayerMap;

    /** Config preferences key. */
    @XmlTransient
    private static final String PREFERENCES_KEY = "SldConfiguration";

    /**
     * Instantiates a new SLD configuration.
     */
    public SldConfiguration()
    {
        // empty constructor
    }

    /**
     * Constructor that takes a layer map.
     *
     * @param newMap the new map
     */
    public SldConfiguration(Map<String, List<StyledLayerDescriptor>> newMap)
    {
        myLayerMap = new HashMap<>(newMap);
    }

    /**
     * Gets a copy of the internal layer map.
     *
     * @return the layer map
     */
    public Map<String, List<StyledLayerDescriptor>> getLayerMap()
    {
        return myLayerMap == null ? null : new HashMap<String, List<StyledLayerDescriptor>>(myLayerMap);
    }

    /**
     * Load config.
     *
     * @param toolbox the file name
     */
    public void loadConfig(Toolbox toolbox)
    {
        SldConfiguration aLayerSetConfig = toolbox.getPreferencesRegistry().getPreferences(SldConfiguration.class)
                .getJAXBObject(SldConfiguration.class, PREFERENCES_KEY, new SldConfiguration());

        myLayerMap = aLayerSetConfig.myLayerMap;
    }

    /**
     * Write the SLD config to the specified filename.
     *
     * @param toolbox the fileName
     * @throws JAXBException an exception that gets thrown if there was any
     *             problem writing the config to the specified filename
     */
    public void writeSldConfig(Toolbox toolbox) throws JAXBException
    {
        toolbox.getPreferencesRegistry().getPreferences(SldConfiguration.class).putJAXBObject(PREFERENCES_KEY, this, false, this);
    }
}
