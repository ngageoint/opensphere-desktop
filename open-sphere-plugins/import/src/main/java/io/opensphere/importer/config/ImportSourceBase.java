package io.opensphere.importer.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;

/** Base class for an imported source. */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ImportSourceBase extends AbstractDataSource implements Cloneable
{
    /** The data source URI string. */
    @XmlElement(name = "sourceUri", required = true)
    protected String mySourceUri;

    /** The visibility flag. */
    @XmlElement(name = "visible")
    protected boolean myIsVisible = true;

    /** Whether this is from a state. */
    @XmlElement(name = "fromState")
    protected boolean myIsFromState;

    /** The load error. */
    @XmlTransient
    protected boolean myLoadError;

    // ---------- Begin variables that should probably not be in here ----------

    /** The data group info. */
    @XmlTransient
    protected DataGroupInfo myDataGroupInfo;

    /** The data type info. */
    @XmlTransient
    protected DataTypeInfo myDataTypeInfo;

    /** The my participating (in what?). */
    @XmlTransient
    protected boolean myParticipating;

    /**
     * Generate type key.
     *
     * @return the string
     */
    public abstract String generateTypeKey();

    /**
     * Provide abstract access to LayerSettings or a subclass implementation.
     * @return the LayerSettings
     */
    protected abstract LayerSettings getLayer();

    /**
     * Gets the source URI.
     *
     * @return the source URI
     */
    public URI getSourceUri()
    {
        try
        {
            return new URI(mySourceUri);
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }

    /**
     * Gets the source URI as a String.
     *
     * @return the source URI
     */
    public String getSourceUriString()
    {
        return mySourceUri;
    }

    /**
     * Sets the source URI.
     *
     * @param sourceUri the source URI
     */
    public void setSourceUri(URI sourceUri)
    {
        mySourceUri = sourceUri.toString();
    }

    /**
     * Checks if this source is visible or not.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myIsVisible;
    }

    /**
     * Sets the visibility flag.
     *
     * @param isVisible the new visible
     */
    public void setVisible(boolean isVisible)
    {
        myIsVisible = isVisible;
    }

    /**
     * True if this is a state source.
     *
     * @return true, if is from state source
     */
    public boolean isFromState()
    {
        return myIsFromState;
    }

    /**
     * Marks this source as one that is associated with a saved state.
     *
     * @param fromStateSource the new from state source
     */
    public void setFromState(boolean fromStateSource)
    {
        myIsFromState = fromStateSource;
    }

    /**
     * Gets the data group info.
     *
     * @return the data group info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDataGroupInfo;
    }

    /**
     * Sets the data group info.
     *
     * @param dgi the new data group info
     */
    public void setDataGroupInfo(DataGroupInfo dgi)
    {
        myDataGroupInfo = dgi;
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Sets the data type info.
     *
     * @param dti the new data type info
     */
    public void setDataTypeInfo(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    /**
     * Checks if is participating.
     *
     * @return true, if is participating
     */
    public boolean isParticipating()
    {
        return myParticipating;
    }

    /**
     * Sets the participating.
     *
     * @param participating the new participating
     */
    public void setParticipating(boolean participating)
    {
        myParticipating = participating;
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public boolean isActive()
    {
        return getLayer().isActive();
    }

    @Override
    public void setActive(boolean active)
    {
        getLayer().setActive(active);
    }

    @Override
    public String getName()
    {
        return getLayer().getName();
    }

    @Override
    public void setName(String name)
    {
        getLayer().setName(name);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(mySourceUri);
        result = prime * result + HashCodeHelper.getHashCode(getLayer());
        result = prime * result + HashCodeHelper.getHashCode(myIsVisible);
        result = prime * result + HashCodeHelper.getHashCode(myIsFromState);
        return result;
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
        ImportSourceBase other = (ImportSourceBase)obj;
        //@formatter:off
        return Objects.equals(mySourceUri, other.mySourceUri)
                && Objects.equals(getLayer(), other.getLayer())
                && myIsVisible == other.myIsVisible
                && myIsFromState == other.myIsFromState;
        //@formatter:on
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append("ImportDataSource [sourceUri=");
        builder.append(mySourceUri);
        builder.append(", layerSettings=");
        builder.append(getLayer());
        builder.append(", isVisible=");
        builder.append(myIsVisible);
        builder.append(", fromStateSource=");
        builder.append(myIsFromState);
        builder.append(", loadError=");
        builder.append(myLoadError);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public ImportSourceBase clone()
    {
        try
        {
            return (ImportSourceBase)super.clone();
        }
        catch (CloneNotSupportedException eek)
        {
            // Not possible
        }
        return null;
    }

    /**
     * Converts a URI into a "normal" string.
     *
     * @param uri the URI
     * @return the string
     */
    public static String toString(URI uri)
    {
        return "file".equals(uri.getScheme()) ? new File(uri.getSchemeSpecificPart()).getAbsolutePath() : uri.toString();
    }
}
