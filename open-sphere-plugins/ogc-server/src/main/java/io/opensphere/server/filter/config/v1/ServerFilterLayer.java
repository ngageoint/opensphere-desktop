package io.opensphere.server.filter.config.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class ServerFilterLayer.
 */
@XmlRootElement(name = "Layer")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerFilterLayer
{
    /** The WMS layer name. */
    @XmlAttribute(name = "wmsName")
    private String myWmsLayerName;

    /** The WFS layer name. */
    @XmlAttribute(name = "wfsName")
    private String myWfsLayerName;

    /** The layer title. */
    @XmlAttribute(name = "title")
    private String myLayerTitle;

    /**
     * Default Constructor.
     */
    public ServerFilterLayer()
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ServerFilterLayer other = (ServerFilterLayer)obj;
        return EqualsHelper.equals(myWmsLayerName, other.myWmsLayerName, myWfsLayerName, other.myWfsLayerName, myLayerTitle,
                other.myLayerTitle);
    }

    /**
     * Gets the layer title.
     *
     * @return the layer title
     */
    public String getLayerTitle()
    {
        return myLayerTitle;
    }

    /**
     * Gets the layer name.
     *
     * @return the layer name
     */
    public String getWfsLayerName()
    {
        return myWfsLayerName;
    }

    /**
     * Gets the layer name.
     *
     * @return the layer name
     */
    public String getWmsLayerName()
    {
        return myWmsLayerName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myWmsLayerName == null ? 0 : myWmsLayerName.hashCode());
        result = prime * result + (myWfsLayerName == null ? 0 : myWfsLayerName.hashCode());
        result = prime * result + (myLayerTitle == null ? 0 : myLayerTitle.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [Title=\"").append(myLayerTitle).append("\" WMSName=\"");
        sb.append(myWmsLayerName).append("\" WFSName=\"");
        sb.append(myWfsLayerName).append("\"]");
        return sb.toString();
    }
}
