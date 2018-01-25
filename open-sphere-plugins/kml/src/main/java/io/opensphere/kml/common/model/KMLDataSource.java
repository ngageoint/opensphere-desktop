package io.opensphere.kml.common.model;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Schema;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.SingleFileDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;

/**
 * Configuration of a single KML file source.
 */
@XmlRootElement(name = "KMLDataSource")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class KMLDataSource extends AbstractDataSource implements SingleFileDataSource, Cloneable
{
    /** The name. */
    @XmlElement(name = "name")
    private String myName = "";

    /** Whether this is active. */
    @XmlElement(name = "active")
    private volatile boolean myActive;

    /** The visibility flag. */
    @XmlElement(name = "visible")
    private boolean myIsVisible = true;

    /** Whether there was a load error. */
    @XmlTransient
    private boolean myLoadError;

    /** The KML file absolute path. */
    @XmlElement(name = "KMLFileAbsolutePath", required = true)
    private String myPath = "";

    /** Whether this uses a URL. Kept for legacy compatibility. */
    @XmlAttribute(name = "usesURL", required = false)
    private boolean myUsesURL;

    /** The type of data source (where to read the file). */
    @XmlAttribute(name = "type", required = false)
    private Type myType;

    /** Whether to include data in the timeline. */
    @XmlAttribute(name = "includeInTimeline", required = false)
    private boolean myIncludeInTimeline = true;

    /** Whether to use icons when provided (false means use dots). */
    @XmlAttribute(name = "useIcons", required = false)
    private boolean myUseIcons = true;

    /** Whether to clamp features to the terrain. */
    @XmlAttribute(name = "clampToTerrain", required = false)
    private boolean myClampToTerrain;

    /** The optional refresh rate (in seconds). */
    @XmlElement(name = "refreshRate", required = false)
    private int myRefreshRateSeconds;

    /** Whether to fill polygons that don't have a fill specified. */
    @XmlElement(name = "polygonsFilled", required = false)
    private boolean myPolygonsFilled;

    /** Whether to show labels. */
    @XmlElement(name = "showLabels", required = false)
    private boolean myShowLabels = true;

    /** The From state source. */
    @XmlAttribute(name = "fromStateSource")
    private boolean myFromStateSource;

    /** The scaling method. */
    @XmlElement(name = "scalingMethod", required = false)
    private ScalingMethod myScalingMethod = ScalingMethod.DEFAULT;

    /** The data group for this data source. */
    @XmlTransient
    private DataGroupInfo myDataGroupInfo;

    /** Whether this is a style data source. */
    @XmlTransient
    private boolean myIsStyleSource;

    /** An optional data group key to use instead of the default. */
    @XmlTransient
    private String myOverrideDataGroupKey;

    /** Parent data source. */
    @XmlTransient
    private KMLDataSource myParentDataSource;

    /** Child data sources. */
    @XmlTransient
    private Collection<KMLDataSource> myChildDataSources;

    /**
     * Optional feature that created this data source (for network links,
     * overlays, etc).
     */
    @XmlTransient
    private KMLFeature myCreatingFeature;

    /** Optional root feature that was created from this data source. */
    @XmlTransient
    private KMLFeature myResultingFeature;

    /** The content type. This could be a guess or actual content type. */
    @XmlTransient
    private KMLContentType myContentType;

    /** The actual path (including any query parameters). */
    @XmlTransient
    private String myActualPath;

    /** The reason this data source failed to load. */
    @XmlTransient
    private FailureReason myFailureReason;

    /** The error message if this data source failed to load. */
    @XmlTransient
    private String myErrorMessage;

    /** Optional Schemas when reloading a data source with Schemas. */
    @XmlTransient
    private Collection<? extends Schema> mySchemata;

    /** The handler associated with this data source. */
    @XmlTransient
    private Object myHandler;

    /** The HTTP response headers. */
    @XmlTransient
    private Map<String, String> myResponseHeaders;

    /** The outcome tracker. */
    @XmlTransient
    private final OutcomeTracker myOutcomeTracker = new OutcomeTracker();

    /**
     * Associate this data source with a handler.
     *
     * @param handler The handler.
     */
    public synchronized void associateEnvoy(Object handler)
    {
        if (myHandler != null && Utilities.notSameInstance(myHandler, handler))
        {
            throw new IllegalStateException("This data source is already associated with handler: " + myHandler);
        }
        myHandler = handler;
        notifyAll();
    }

    @Override
    public KMLDataSource clone()
    {
        KMLDataSource clone;
        try
        {
            clone = (KMLDataSource)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
        return clone;
    }

    /**
     * Creates a near copy of this data source suitable for export.
     *
     * @return An exportable copy of this data source
     */
    public KMLDataSource createExportDataSource()
    {
        KMLDataSource copySource = new KMLDataSource();
        copySource.setName(getName());
        copySource.setPath(getPath());
        copySource.setType(getType());
        copySource.setIncludeInTimeline(isIncludeInTimeline());
        copySource.setRefreshRate(getRefreshRate());
        copySource.setUseIcons(isUseIcons());
        copySource.setClampToTerrain(isClampToTerrain());
        return copySource;
    }

    /**
     * Disassociate a handler for this data source.
     *
     * @param handler The handler
     */
    public synchronized void disassociateHandler(Object handler)
    {
        myHandler = null;
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
        KMLDataSource other = (KMLDataSource)obj;
        return EqualsHelper.equals(myName, other.myName, myPath, other.myPath, myCreatingFeature, other.myCreatingFeature);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(myName, myPath, myCreatingFeature);
    }

    @Override
    public boolean exportsAsBundle()
    {
        return getType() == Type.FILE;
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, final ActionListener callback)
    {
        KMLExportDataSourceHelper.exportToFile(this, selectedFile, callback);
    }

    /**
     * Getter for actualPath.
     *
     * @return the actualPath
     */
    public String getActualPath()
    {
        if (myActualPath == null)
        {
            myActualPath = getPath();
        }
        return myActualPath;
    }

    /**
     * Gets all the data sources under this data source (recursively).
     *
     * @return all the data sources
     */
    public Collection<KMLDataSource> getAllChildDataSources()
    {
        Collection<KMLDataSource> allDataSources = New.list();
        accumulateChildDataSources(allDataSources);
        return allDataSources;
    }

    /**
     * Gets all the data sources under this data source, including itself
     * (recursively).
     *
     * @return all the data sources
     */
    public Collection<KMLDataSource> getAllDataSources()
    {
        Collection<KMLDataSource> allDataSources = New.list();
        allDataSources.add(this);
        accumulateChildDataSources(allDataSources);
        return allDataSources;
    }

    /**
     * Returns the list of all features under this data source.
     *
     * @return The list of all features
     */
    public Collection<KMLFeature> getAllFeatures()
    {
        return myResultingFeature != null ? myResultingFeature.getAllFeatures() : New.<KMLFeature>list();
    }

    /**
     * Getter for childDataSources.
     *
     * @return the childDataSources
     */
    public Collection<KMLDataSource> getChildDataSources()
    {
        if (myChildDataSources == null)
        {
            myChildDataSources = New.list();
        }
        return myChildDataSources;
    }

    /**
     * Getter for contentType.
     *
     * @return the contentType
     */
    public KMLContentType getContentType()
    {
        return myContentType;
    }

    /**
     * Gets the creating feature.
     *
     * @return the creating feature
     */
    public Feature getCreatingFeature()
    {
        return myCreatingFeature != null ? myCreatingFeature.getFeature() : null;
    }

    /**
     * Gets the creating feature.
     *
     * @return the creating feature
     */
    public KMLFeature getCreatingKMLFeature()
    {
        return myCreatingFeature;
    }

    /**
     * Getter for dataGroupInfo.
     *
     * @return the dataGroupInfo
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDataGroupInfo;
    }

    /**
     * Returns the data group key for this data source.
     *
     * @return The data group key
     */
    public String getDataGroupKey()
    {
        if (myOverrideDataGroupKey != null)
        {
            return myOverrideDataGroupKey;
        }
        return StringUtilities.concat("KML/", getRootDataSource().getName());
    }

    /**
     * Returns the data type key for this data source.
     *
     * @return The data type key
     */
    public String getDataTypeKey()
    {
        return getDataGroupKey();
    }

    /**
     * Getter for errorMessage.
     *
     * @return the errorMessage
     */
    public String getErrorMessage()
    {
        return myErrorMessage;
    }

    /**
     * Getter for failureReason.
     *
     * @return the failureReason
     */
    public FailureReason getFailureReason()
    {
        return myFailureReason;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the outcome tracker.
     *
     * @return the outcome tracker
     */
    public OutcomeTracker getOutcomeTracker()
    {
        return myOutcomeTracker;
    }

    /**
     * Getter for overrideDataGroupKey.
     *
     * @return the overrideDataGroupKey
     */
    public String getOverrideDataGroupKey()
    {
        return myOverrideDataGroupKey;
    }

    /**
     * Getter for parentDataSource.
     *
     * @return the parentDataSource
     */
    public KMLDataSource getParentDataSource()
    {
        return myParentDataSource;
    }

    @Override
    public String getPath()
    {
        return myPath;
    }

    /**
     * Gets the refresh rate (should be seconds).
     *
     * @return The refresh rate.
     */
    public int getRefreshRate()
    {
        return myRefreshRateSeconds;
    }

    /**
     * Gets the response headers.
     *
     * @return the response headers
     */
    public Map<String, String> getResponseHeaders()
    {
        if (myResponseHeaders == null)
        {
            myResponseHeaders = New.map();
        }
        return myResponseHeaders;
    }

    /**
     * Gets the root data source.
     *
     * @return The root data source
     */
    public KMLDataSource getRootDataSource()
    {
        KMLDataSource rootDataSource = this;
        while (rootDataSource.getParentDataSource() != null)
        {
            rootDataSource = rootDataSource.getParentDataSource();
        }
        return rootDataSource;
    }

    /**
     * Getter for schemata.
     *
     * @return the schemata
     */
    public Collection<? extends Schema> getSchemata()
    {
        return mySchemata == null ? Collections.<Schema>emptySet() : mySchemata;
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public Type getType()
    {
        if (myType == null)
        {
            myType = myUsesURL ? Type.URL : Type.FILE;
        }
        return myType;
    }

    @Override
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Getter for clampToTerrain.
     *
     * @return the clampToTerrain
     */
    public boolean isClampToTerrain()
    {
        return myClampToTerrain;
    }

    /**
     * True if this is a state source.
     *
     * @return true, if is from state source
     */
    public boolean isFromStateSource()
    {
        return myFromStateSource;
    }

    /**
     * Getter for includeInTimeline.
     *
     * @return the includeInTimeline
     */
    public boolean isIncludeInTimeline()
    {
        return myIncludeInTimeline;
    }

    /**
     * Getter for polygonsFilled.
     *
     * @return the polygonsFilled
     */
    public boolean isPolygonsFilled()
    {
        return myPolygonsFilled;
    }

    /**
     * Getter for showLabels.
     *
     * @return the showLabels
     */
    public boolean isShowLabels()
    {
        return myShowLabels;
    }

    /**
     * Getter for isStyleSource.
     *
     * @return the isStyleSource
     */
    public boolean isStyleSource()
    {
        return myIsStyleSource;
    }

    /**
     * Getter for useIcons.
     *
     * @return the useIcons
     */
    public boolean isUseIcons()
    {
        return myUseIcons;
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myIsVisible;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public void setActive(boolean active)
    {
        myActive = active;
        if (!active)
        {
            for (KMLDataSource child : getAllChildDataSources())
            {
                child.setActive(false);
            }
        }
    }

    /**
     * Setter for actualPath.
     *
     * @param actualPath the actualPath
     */
    public void setActualPath(String actualPath)
    {
        myActualPath = actualPath;
    }

    /**
     * Setter for clampToTerrain.
     *
     * @param clampToTerrain the clampToTerrain
     */
    public void setClampToTerrain(boolean clampToTerrain)
    {
        myClampToTerrain = clampToTerrain;
    }

    /**
     * Setter for contentType.
     *
     * @param contentType the contentType
     */
    public void setContentType(KMLContentType contentType)
    {
        myContentType = contentType;
    }

    /**
     * Sets the creating feature.
     *
     * @param creatingFeature the new creating feature
     */
    public void setCreatingKMLFeature(KMLFeature creatingFeature)
    {
        myCreatingFeature = creatingFeature;
    }

    /**
     * Setter for dataGroupInfo.
     *
     * @param dataGroupInfo the dataGroupInfo
     */
    public void setDataGroupInfo(DataGroupInfo dataGroupInfo)
    {
        myDataGroupInfo = dataGroupInfo;
    }

    /**
     * Setter for errorMessage.
     *
     * @param errorMessage the errorMessage
     */
    public void setErrorMessage(String errorMessage)
    {
        myErrorMessage = errorMessage;
    }

    /**
     * Setter for failureReason.
     *
     * @param failureReason the failureReason
     */
    public void setFailureReason(FailureReason failureReason)
    {
        myFailureReason = failureReason;
    }

    /**
     * Marks this source as one that is associated with a saved state.
     *
     * @param fromStateSource the new from state source
     */
    public void setFromStateSource(boolean fromStateSource)
    {
        myFromStateSource = fromStateSource;
    }

    /**
     * Setter for includeInTimeline.
     *
     * @param includeInTimeline the includeInTimeline
     */
    public void setIncludeInTimeline(boolean includeInTimeline)
    {
        myIncludeInTimeline = includeInTimeline;
    }

    /**
     * Gets the scaling method.
     *
     * @return the scaling method
     */
    public ScalingMethod getScalingMethod()
    {
        return myScalingMethod;
    }

    /**
     * Sets the scaling method.
     *
     * @param scalingMethod the scaling method
     */
    public void setScalingMethod(ScalingMethod scalingMethod)
    {
        myScalingMethod = scalingMethod;
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Setter for overrideDataGroupKey.
     *
     * @param overrideDataGroupKey the overrideDataGroupKey
     */
    public void setOverrideDataGroupKey(String overrideDataGroupKey)
    {
        myOverrideDataGroupKey = overrideDataGroupKey;
    }

    /**
     * Setter for parentDataSource.
     *
     * @param parentDataSource the parentDataSource
     */
    public void setParentDataSource(KMLDataSource parentDataSource)
    {
        myParentDataSource = parentDataSource;
    }

    @Override
    public void setPath(String path)
    {
        myPath = path;
    }

    /**
     * Setter for polygonsFilled.
     *
     * @param polygonsFilled the polygonsFilled
     */
    public void setPolygonsFilled(boolean polygonsFilled)
    {
        myPolygonsFilled = polygonsFilled;
    }

    /**
     * Sets the refresh rate (should be seconds).
     *
     * @param rate The refresh rate.
     */
    public void setRefreshRate(int rate)
    {
        myRefreshRateSeconds = rate;
    }

    /**
     * Sets the resulting feature.
     *
     * @param resultingFeature the new resulting feature
     */
    public void setResultingFeature(KMLFeature resultingFeature)
    {
        myResultingFeature = resultingFeature;
    }

    /**
     * Setter for schemata.
     *
     * @param schemata the schemata
     */
    public void setSchemata(Collection<Schema> schemata)
    {
        mySchemata = schemata;
    }

    /**
     * Setter for showLabels.
     *
     * @param showLabels the showLabels
     */
    public void setShowLabels(boolean showLabels)
    {
        myShowLabels = showLabels;
    }

    /**
     * Setter for isStyleSource.
     *
     * @param isStyleSource the isStyleSource
     */
    public void setStyleSource(boolean isStyleSource)
    {
        myIsStyleSource = isStyleSource;
    }

    /**
     * Setter for type.
     *
     * @param type the type
     */
    public void setType(Type type)
    {
        myType = type;
    }

    /**
     * Setter for useIcons.
     *
     * @param useIcons the useIcons
     */
    public void setUseIcons(boolean useIcons)
    {
        myUseIcons = useIcons;
    }

    /**
     * Sets the visible.
     *
     * @param visible the new visible
     */
    public void setVisible(boolean visible)
    {
        myIsVisible = visible;
    }

    @Override
    public boolean supportsFileExport()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "KMLDataSource [name=" + myName + ", path=" + myPath + ", type=" + getType() + ", isActive=" + isActive() + "]";
    }

    @Override
    public void updateDataLocations(File destDataDir)
    {
        setPath(destDataDir.getAbsolutePath() + File.separator + getPath());
    }

    /**
     * Wait for the handler for this data source to be associated.
     *
     * @param timeoutMillis If non-zero, only wait this number of milliseconds
     *            for the handler.
     */
    public synchronized void waitForHandler(long timeoutMillis)
    {
        long startMillis = System.currentTimeMillis();
        while (myHandler == null && (timeoutMillis == 0L || System.currentTimeMillis() - startMillis < timeoutMillis))
        {
            try
            {
                wait(timeoutMillis);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Gets all the data sources under this data source (recursively).
     *
     * @param allDataSources list of data sources to build up
     */
    private void accumulateChildDataSources(Collection<KMLDataSource> allDataSources)
    {
        if (myChildDataSources != null)
        {
            allDataSources.addAll(myChildDataSources);
            for (KMLDataSource child : myChildDataSources)
            {
                child.accumulateChildDataSources(allDataSources);
            }
        }
    }

    /** The reason this data source failed to load. */
    public enum FailureReason
    {
        /** Invalid certificate. */
        INVALID_CERTIFICATE,

        /** Invalid basic auth. */
        INVALID_BASIC_AUTH,

        /** Invalid - either basic auth or certificate. */
        INVALID_EITHER,

        /** Namespace parsing error. */
        NAMESPACE_PARSE_ERROR,

        /** Other reason. */
        OTHER;
    }

    /** The type of data source (where to read the file). */
    public enum Type
    {
        /** File data source type. */
        FILE,

        /** URL data source type. */
        URL;
    }
}
