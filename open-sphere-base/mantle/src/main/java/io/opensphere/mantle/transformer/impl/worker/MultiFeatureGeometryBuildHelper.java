package io.opensphere.mantle.transformer.impl.worker;

import java.util.Map;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DefaultFeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.impl.DefaultFeatureIndividualGeometryBuilderData;

/**
 * The Class MultiFeatureGeometryBuildHelper.
 *
 * A helper class that collects up the individual builders and uses them to
 * build combined builders for use with combined element types.
 */
public class MultiFeatureGeometryBuildHelper
{
    /** The Style to builder data map. */
    private final Map<FeatureVisualizationStyle, DefaultFeatureCombinedGeometryBuilderData> myStyleToBuilderDataMap;

    /**
     * Instantiates a new multi feature geometry build helper.
     */
    public MultiFeatureGeometryBuildHelper()
    {
        myStyleToBuilderDataMap = New.map();
    }

    /**
     * Adds the builder data.
     *
     * @param style the style
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     */
    public void addBuilderData(FeatureVisualizationStyle style, FeatureIndividualGeometryBuilderData bd)
    {
        if (bd != null && bd.getVS().isVisible())
        {
            DefaultFeatureCombinedGeometryBuilderData cbd = myStyleToBuilderDataMap.get(style);
            if (cbd == null)
            {
                cbd = new DefaultFeatureCombinedGeometryBuilderData();
                myStyleToBuilderDataMap.put(style, cbd);
            }
            cbd.addBuidler(new DefaultFeatureIndividualGeometryBuilderData(bd));
        }
    }

    /**
     * Disposes of all helper data.
     */
    public void clear()
    {
        myStyleToBuilderDataMap.clear();
    }

    /**
     * Creates the multi feature geometries and adds them to the visible
     * geometry set.
     *
     * @param visSet the vis set to add the geometries too.
     * @param renderPropertyPool the render property pool
     */
    public void createMultiFeatureGeometries(Set<Geometry> visSet, RenderPropertyPool renderPropertyPool)
    {
        for (Map.Entry<FeatureVisualizationStyle, DefaultFeatureCombinedGeometryBuilderData> entry : myStyleToBuilderDataMap
                .entrySet())
        {
            entry.getKey().createCombinedGeometry(visSet, entry.getValue(), renderPropertyPool);
        }
    }
}
