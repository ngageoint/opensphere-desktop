package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.units.length.Length;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;

/**
 * A mock ellipse feature style class used for tests.
 */
public class MockFeatureStyle extends AbstractEllipseFeatureVisualizationStyle
{
    /**
     * The length units of the axis.
     */
    private final Class<? extends Length> myAxisUnits;

    /**
     * The style's color.
     */
    private final Color myColor;

    /**
     * Constructs a new mock ellipse style.
     *
     * @param toolbox A mocked {@link Toolbox}.
     * @param color The color of the style.
     * @param axisUnits The length units of the axis.
     */
    public MockFeatureStyle(Toolbox toolbox, Color color, Class<? extends Length> axisUnits)
    {
        super(toolbox);
        myColor = color;
        myAxisUnits = axisUnits;
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        return null;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return null;
    }

    @Override
    public Class<? extends Length> getAxisUnit()
    {
        return myAxisUnits;
    }

    @Override
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public Class<? extends VisualizationSupport> getConvertedClassType()
    {
        return null;
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return null;
    }

    @Override
    public String getStyleDescription()
    {
        return null;
    }

    @Override
    public String getStyleName()
    {
        return null;
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        return null;
    }

    @Override
    public boolean supportsLabels()
    {
        return false;
    }
}
