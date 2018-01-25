package io.opensphere.wps.layer;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * A builder implementation allowing complex data types to be constructed
 * (accreted) over time.
 */
public class WpsDataTypeInfoBuilder
{
    /**
     * The toolbox through which application interaction occurs.
     */
    private Toolbox myToolbox;

    /**
     * The title of the server.
     */
    private String myServerTitle;

    /**
     * The name of the type key used in new data types.
     */
    private String myProcessKey;

    /**
     * The name of the type key used in new data types.
     */
    private MetaDataInfo myMetadataInfo;

    /**
     * The process ID used in new data types.
     */
    private String myProcessId;

    /**
     * The name of the type used in new data types.
     */
    private String myTypeName;

    /**
     * The URL used in new data types.
     */
    private String myUrl;

    /**
     * The description used in new data types.
     */
    private String myDescription;

    /**
     * The visualization type used in new data types.
     */
    private MapVisualizationType myVisualizationType;

    /**
     * The loads-to value used in new data types.
     */
    private LoadsTo myLoadsTo;

    /**
     * The name of the geometry column used in new data types.
     */
    private String myGeometryColumn;

    /**
     * The display name used in new data types.
     */
    private String myDisplayName;

    /**
     * Builds a new data type, based on the configured values.
     *
     * @param instanceId The id of this wps instance.
     * @return a new data type, configured using the values supplied to the
     *         builder.
     */
    public WpsDataTypeInfo build(String instanceId)
    {
        WpsDataTypeInfo dataType = new WpsDataTypeInfo(myToolbox, getServerTitle(),
                getProcessKey() + "!!" + getDisplayName() + "!!" + instanceId, getTypeName(), getDisplayName(),
                getMetadataInfo());

        dataType.getMapVisualizationInfo().setVisualizationType(getVisualizationType());
        dataType.getBasicVisualizationInfo().setLoadsTo(getLoadsTo(), this);
        dataType.setProcessId(getProcessId());
        dataType.setUrl(getUrl());
        dataType.getMetaDataInfo().setGeometryColumn(getGeometryColumn());
        dataType.setDescription(getDescription());

        return dataType;
    }

    /**
     * Sets the value of the {@link #myToolbox} field.
     *
     * @param pToolbox the value to store in the {@link #myToolbox} field.
     */
    public void setToolbox(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
    }

    /**
     * Gets the value of the {@link #myToolbox} field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the value of the {@link #myServerTitle} field.
     *
     * @return the value stored in the {@link #myServerTitle} field.
     */
    public String getServerTitle()
    {
        return myServerTitle;
    }

    /**
     * Sets the value of the {@link #myServerTitle} field.
     *
     * @param pServerTitle the value to store in the {@link #myServerTitle}
     *            field.
     */
    public void setServerTitle(String pServerTitle)
    {
        myServerTitle = pServerTitle;
    }

    /**
     * Gets the value of the {@link #myProcessKey} field.
     *
     * @return the value stored in the {@link #myProcessKey} field.
     */
    public String getProcessKey()
    {
        return myProcessKey;
    }

    /**
     * Sets the value of the {@link #myProcessKey} field.
     *
     * @param processKey the value to store in the {@link #myProcessKey} field.
     */
    public void setProcessKey(String processKey)
    {
        myProcessKey = processKey;
    }

    /**
     * Gets the value of the {@link #myMetadataInfo} field.
     *
     * @return the value stored in the {@link #myMetadataInfo} field.
     */
    public MetaDataInfo getMetadataInfo()
    {
        return myMetadataInfo;
    }

    /**
     * Sets the value of the {@link #myMetadataInfo} field.
     *
     * @param pMetadataInfo the value to store in the {@link #myMetadataInfo}
     *            field.
     */
    public void setMetadataInfo(MetaDataInfo pMetadataInfo)
    {
        myMetadataInfo = pMetadataInfo;
    }

    /**
     * Gets the value of the {@link #myProcessId} field.
     *
     * @return the value stored in the {@link #myProcessId} field.
     */
    public String getProcessId()
    {
        return myProcessId;
    }

    /**
     * Sets the value of the {@link #myProcessId} field.
     *
     * @param pProcessId the value to store in the {@link #myProcessId} field.
     */
    public void setProcessId(String pProcessId)
    {
        myProcessId = pProcessId;
    }

    /**
     * Gets the value of the {@link #myTypeName} field.
     *
     * @return the value stored in the {@link #myTypeName} field.
     */
    public String getTypeName()
    {
        return myTypeName;
    }

    /**
     * Sets the value of the {@link #myTypeName} field.
     *
     * @param pTypeName the value to store in the {@link #myTypeName} field.
     */
    public void setTypeName(String pTypeName)
    {
        myTypeName = pTypeName;
    }

    /**
     * Gets the value of the {@link #myUrl} field.
     *
     * @return the value stored in the {@link #myUrl} field.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Sets the value of the {@link #myUrl} field.
     *
     * @param pUrl the value to store in the {@link #myUrl} field.
     */
    public void setUrl(String pUrl)
    {
        myUrl = pUrl;
    }

    /**
     * Gets the value of the {@link #myDescription} field.
     *
     * @return the value stored in the {@link #myDescription} field.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Sets the value of the {@link #myDescription} field.
     *
     * @param pDescription the value to store in the {@link #myDescription}
     *            field.
     */
    public void setDescription(String pDescription)
    {
        myDescription = pDescription;
    }

    /**
     * Gets the value of the {@link #myVisualizationType} field.
     *
     * @return the value stored in the {@link #myVisualizationType} field.
     */
    public MapVisualizationType getVisualizationType()
    {
        return myVisualizationType;
    }

    /**
     * Sets the value of the {@link #myVisualizationType} field.
     *
     * @param pVisualizationType the value to store in the
     *            {@link #myVisualizationType} field.
     */
    public void setVisualizationType(MapVisualizationType pVisualizationType)
    {
        myVisualizationType = pVisualizationType;
    }

    /**
     * Gets the value of the {@link #myLoadsTo} field.
     *
     * @return the value stored in the {@link #myLoadsTo} field.
     */
    public LoadsTo getLoadsTo()
    {
        return myLoadsTo;
    }

    /**
     * Sets the value of the {@link #myLoadsTo} field.
     *
     * @param pLoadsTo the value to store in the {@link #myLoadsTo} field.
     */
    public void setLoadsTo(LoadsTo pLoadsTo)
    {
        myLoadsTo = pLoadsTo;
    }

    /**
     * Gets the value of the {@link #myGeometryColumn} field.
     *
     * @return the value stored in the {@link #myGeometryColumn} field.
     */
    public String getGeometryColumn()
    {
        return myGeometryColumn;
    }

    /**
     * Sets the value of the {@link #myGeometryColumn} field.
     *
     * @param pGeometryColumn the value to store in the
     *            {@link #myGeometryColumn} field.
     */
    public void setGeometryColumn(String pGeometryColumn)
    {
        myGeometryColumn = pGeometryColumn;
    }

    /**
     * Gets the value of the {@link #myDisplayName} field.
     *
     * @return the value stored in the {@link #myDisplayName} field.
     */
    public String getDisplayName()
    {
        return myDisplayName;
    }

    /**
     * Sets the value of the {@link #myDisplayName} field.
     *
     * @param pDisplayName the value to store in the {@link #myDisplayName}
     *            field.
     */
    public void setDisplayName(String pDisplayName)
    {
        myDisplayName = pDisplayName;
    }
}
