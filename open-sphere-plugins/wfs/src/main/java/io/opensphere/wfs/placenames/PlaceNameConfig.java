package io.opensphere.wfs.placenames;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;

/**
 * A set of place names as determined by an associated filter for the WFS query.
 */
@XmlRootElement(name = "PlaceNamesConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlaceNameConfig implements Cloneable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PlaceNameConfig.class);

    /** The currently loaded configuration. */
    private static PlaceNameConfig ourCurrentConfig;

    /** File which contains this configuration. */
    private static final String PLACENAMES_CONFIG_FILE = "/placeNamesConfig.xml";

    /** Synchronization lock for operations on the current configuration. */
    private static final Object ourLock = new Object();

    /** Name which uniquely identifies this data set. */
    @XmlElement(name = "PlaceNameLayer")
    private List<PlaceNameLayerConfig> myPlaceNameLayers = new ArrayList<>();

    /**
     * Make the given configuration the current configuration and commit.
     *
     * @param conf The configuration to commit.
     */
    public static void commitConfig(PlaceNameConfig conf)
    {
        if (conf == null)
        {
            return;
        }
        ourCurrentConfig = conf;

        try
        {
            XMLUtilities.writeXMLObject(conf, new File(PlaceNameConfig.class.getResource(PLACENAMES_CONFIG_FILE).getFile()));
        }
        catch (JAXBException e)
        {
            LOGGER.error("Cannot create JAXBContext for configuration.", e);
        }
    }

    /**
     * Get a copy of the configuration.
     *
     * @return the config
     */
    public static PlaceNameConfig getConfig()
    {
        PlaceNameConfig retConf;
        synchronized (ourLock)
        {
            if (ourCurrentConfig == null)
            {
                ourCurrentConfig = loadConfig();
            }

            try
            {
                retConf = ourCurrentConfig.clone();
            }
            catch (CloneNotSupportedException ex)
            {
                LOGGER.error("PlaceNameConfig must be Cloneable");
                retConf = null;
            }
        }

        return retConf;
    }

    /**
     * Retrieve any saved configuration.
     *
     * @return The configuration.
     */
    private static PlaceNameConfig loadConfig()
    {
        PlaceNameConfig result;
        try
        {
            result = XMLUtilities.readXMLObject(PlaceNameConfig.class.getResource(PLACENAMES_CONFIG_FILE), PlaceNameConfig.class);
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to unmarshall WMS configuration", e);
            result = new PlaceNameConfig();
        }

        return result;
    }

    @Override
    public PlaceNameConfig clone() throws CloneNotSupportedException
    {
        PlaceNameConfig config = (PlaceNameConfig)super.clone();
        List<PlaceNameLayerConfig> layers = new ArrayList<>(myPlaceNameLayers.size());
        for (PlaceNameLayerConfig layerConfig : myPlaceNameLayers)
        {
            layers.add(layerConfig.clone());
        }
        config.setPlaceNameLayers(layers);
        return config;
    }

    /**
     * Get the placeNameLayers.
     *
     * @return the placeNameLayers
     */
    public List<PlaceNameLayerConfig> getPlaceNameLayers()
    {
        return myPlaceNameLayers;
    }

    /**
     * Set the placeNameLayers.
     *
     * @param placeNameLayers the placeNameLayers to set
     */
    public void setPlaceNameLayers(List<PlaceNameLayerConfig> placeNameLayers)
    {
        myPlaceNameLayers = placeNameLayers;
    }
}
