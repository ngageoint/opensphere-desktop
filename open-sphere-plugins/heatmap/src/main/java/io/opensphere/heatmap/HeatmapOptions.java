package io.opensphere.heatmap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/** The heat map options. */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement()
public class HeatmapOptions
{
    /** The gradient. */
    @XmlElement(name = "gradient")
    private volatile HeatmapGradients myGradient = HeatmapGradients.RAINBOW;

    /** Whether to export. */
    @XmlElement(name = "export")
    private volatile boolean myExport;

    /** The size of the heatmap radius. */
    @XmlElement(name = "size")
    private volatile int mySize;

    /** The intensity (dots per pixel) of the heatmap. */
    @XmlElement(name = "intensity")
    private volatile int myIntensity;

    /** The layer name. */
    @XmlTransient
    private volatile String myLayerName;

    /**
     * Gets the gradient.
     *
     * @return the gradient
     */
    public HeatmapGradients getGradient()
    {
        return myGradient;
    }

    /**
     * Sets the gradient.
     *
     * @param gradient the gradient
     */
    public void setGradient(HeatmapGradients gradient)
    {
        myGradient = gradient;
    }

    /**
     * Gets the value of the {@link #myIntensity} field.
     *
     * @return the value stored in the {@link #myIntensity} field.
     */
    public int getIntensity()
    {
        return myIntensity;
    }

    /**
     * Sets the value of the {@link #myIntensity} field.
     *
     * @param intensity the value to store in the {@link #myIntensity} field.
     */
    public void setIntensity(int intensity)
    {
        myIntensity = intensity;
    }

    /**
     * Gets the value of the {@link #mySize} field.
     *
     * @return the value stored in the {@link #mySize} field.
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Sets the value of the {@link #mySize} field.
     *
     * @param size the value to store in the {@link #mySize} field.
     */
    public void setSize(int size)
    {
        mySize = size;
    }

    /**
     * Gets the export.
     *
     * @return the export
     */
    public boolean isExport()
    {
        return myExport;
    }

    /**
     * Sets the export.
     *
     * @param export the export
     */
    public void setExport(boolean export)
    {
        myExport = export;
    }

    /**
     * Gets the layerName.
     *
     * @return the layerName
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Sets the layerName.
     *
     * @param layerName the layerName
     */
    public void setLayerName(String layerName)
    {
        myLayerName = layerName;
    }
}
