package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collections;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;

/**
 * The Class PointFeatureVisualizationStyle.
 */
public class LOBGeometryFeatureVisualizationStyle extends AbstractLOBFeatureVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    @SuppressWarnings("hiding")
    public static final String ourPropertyKeyPrefix = "LOBGeometryFeatureVisualizationStyle";

    /**
     * Instantiates a new lOB geometry feature visualization style.
     *
     * @param tb the tb
     */
    public LOBGeometryFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new lOB geometry feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public LOBGeometryFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public LOBGeometryFeatureVisualizationStyle clone()
    {
        return (LOBGeometryFeatureVisualizationStyle)super.clone();
    }

    @Override
    public LOBGeometryFeatureVisualizationStyle deriveForType(String dtiKey)
    {
        LOBGeometryFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapLineOfBearingGeometrySupport.class;
    }

    @Override
    public Set<MapVisualizationType> getRequiredMapVisTypes()
    {
        return Collections.singleton(MapVisualizationType.LOB_ELEMENTS);
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for static line-of-bearing(LOB), where the LOB parameters"
                + " are specified when the feature is inserted into the tool.";
    }

    @Override
    public String getStyleName()
    {
        return "Lines of Bearing";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new LOBGeometryFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    @Override
    public Float getLobOrientation(long elementId, MapGeometrySupport mgs, MetaDataProvider mdi)
    {
        Float result = null;
        if (mgs instanceof MapLineOfBearingGeometrySupport)
        {
            result = Float.valueOf(((MapLineOfBearingGeometrySupport)mgs).getOrientation());
        }
        return result;
    }
}
