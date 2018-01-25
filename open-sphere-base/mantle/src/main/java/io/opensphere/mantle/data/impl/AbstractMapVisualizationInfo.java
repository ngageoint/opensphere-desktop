package io.opensphere.mantle.data.impl;

import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * A support class for describing, at a type level, information relevant for
 * rendering the layer to assist the transformers in building the geometries.
 */
public abstract class AbstractMapVisualizationInfo implements MapVisualizationInfo
{
    /** Reference to owner DataTypInfo. */
    private volatile DataTypeInfo myDataTypeInfo;

    /** The Uses visualization styles. */
    private final boolean myUsesVisualizationStyles;

    /** The visualization type. */
    private volatile MapVisualizationType myVisualizationType;

    /**
     * CTOR with default type color.
     *
     * @param visType - the visualization type
     * @param usesVisualizationStyles the true if this type uses the
     *            visualization styles, false if native styles
     */
    public AbstractMapVisualizationInfo(MapVisualizationType visType, boolean usesVisualizationStyles)
    {
        if (visType == null)
        {
            throw new IllegalArgumentException("Visualization Type Cannot be Null");
        }
        myVisualizationType = visType;
        myUsesVisualizationStyles = usesVisualizationStyles;
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return myVisualizationType;
    }

    @Override
    public boolean isImageTileType()
    {
        return myVisualizationType.isImageTileType();
    }

    @Override
    public boolean isMotionImageryType()
    {
        return myVisualizationType == MapVisualizationType.MOTION_IMAGERY;
    }

    @Override
    public boolean isImageType()
    {
        return myVisualizationType.isImageType();
    }

    @Override
    public boolean isZOrderable()
    {
        return true;
    }

    @Override
    public void setDataTypeInfo(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    @Override
    public void setVisualizationType(MapVisualizationType visType)
    {
        if (visType == null)
        {
            throw new IllegalArgumentException("Visualization Type Cannot be Null");
        }
        myVisualizationType = visType;
    }

    @Override
    public String toString()
    {
        return toStringMultiLine(1);
    }

    /**
     * Returns a multi-line string representation of the object.
     *
     * @param indentLevel the indent level (0-based)
     * @return the multi-line string
     */
    public String toStringMultiLine(int indentLevel)
    {
        ToStringHelper helper = new ToStringHelper(this, 128);
        helper.add("MapVisualizationType", myVisualizationType);
        helper.add("Z-Order", getZOrder());
        helper.addIfNotNull("TileLevelController", getTileLevelController());
        return helper.toStringMultiLine(indentLevel);
    }

    @Override
    public boolean usesMapDataElements()
    {
        return myVisualizationType.isMapDataElementType();
    }

    @Override
    public boolean usesVisualizationStyles()
    {
        return myUsesVisualizationStyles;
    }
}
