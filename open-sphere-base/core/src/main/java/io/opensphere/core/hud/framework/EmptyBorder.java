package io.opensphere.core.hud.framework;

import java.util.Collections;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;

/**
 * A border with no size and no geometries.
 */
public class EmptyBorder implements Border
{
    @Override
    public int getBottomInset()
    {
        return 0;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        return Collections.emptySet();
    }

    @Override
    public int getLeftInset()
    {
        return 0;
    }

    @Override
    public int getRightInset()
    {
        return 0;
    }

    @Override
    public int getTopInset()
    {
        return 0;
    }

    @Override
    public void init()
    {
    }
}
