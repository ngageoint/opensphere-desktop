package io.opensphere.core.projection.impl;

import io.opensphere.core.projection.GeographicProjectionModel;
import io.opensphere.core.projection.MutableGeographicProjection;
import io.opensphere.core.terrain.TriangleGlobeModel;

/**
 * A 3-D projection that incorporates terrain.
 */
public class MutableGlobeProjection extends MutableGeographicProjection
{
    /**
     * Construct the projection.
     */
    public MutableGlobeProjection()
    {
        super(new TriangleGlobeModel(13, 50, new Earth3D()));
    }

    @Override
    public void generateSnapshot()
    {
        getModel().generateModelSnapshot();
        GeographicProjectionModel globe = getModel().getModelSnapshot();
        setSnapshot(new ImmutableGlobeProjection(globe, getModelCenter()));
    }

    @Override
    public String getName()
    {
        return "3-D";
    }
}
