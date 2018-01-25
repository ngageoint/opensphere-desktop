package io.opensphere.core.projection.impl;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.projection.GeographicProjectionModel;
import io.opensphere.core.projection.ImmutableGeographicProjection;

/**
 * A 3-D projection that incorporates terrain.
 */
public class ImmutableGlobeProjection extends ImmutableGeographicProjection
{
    /**
     * Create a terrain projection which uses the given globe.
     *
     * @param model the globe which backs this projection.
     * @param modelCenter The center for eye coordinates which should be used
     *            with this projection.
     */
    public ImmutableGlobeProjection(GeographicProjectionModel model, Vector3d modelCenter)
    {
        super(model, modelCenter);
    }

    @Override
    public String getName()
    {
        return "3-D";
    }
}
