package io.opensphere.importer.config;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import io.opensphere.mantle.data.util.LayerUtils;

/** Data source configuration for an imported source. */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ImportDataSource extends ImportSourceBase
{
    /** The layer settings. */
    @XmlElement(name = "layerSettings", required = true)
    private LayerSettings myLayerSettings;

    /** JAXB Constructor. */
    public ImportDataSource()
    {
    }

    /**
     * Constructor.
     *
     * @param sourceUri The data source URI
     */
    public ImportDataSource(URI sourceUri)
    {
        setSourceUri(sourceUri);
        myLayerSettings = new LayerSettings(LayerUtils.getLayerName(sourceUri));
    }

    @Override
    protected LayerSettings getLayer()
    {
        return myLayerSettings;
    }

    /**
     * Gets the layerSettings.
     *
     * @return the layerSettings
     */
    public LayerSettings getLayerSettings()
    {
        return myLayerSettings;
    }

    /**
     * Sets the layerSettings.
     *
     * @param layerSettings the layerSettings
     */
    public void setLayerSettings(LayerSettings layerSettings)
    {
        myLayerSettings = layerSettings;
    }

    @Override
    public ImportDataSource clone()
    {
        ImportDataSource result = (ImportDataSource)super.clone();
        result.myLayerSettings = myLayerSettings.clone();
        return result;
    }
}
