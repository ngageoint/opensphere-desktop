package io.opensphere.core.hud.framework;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;

/**
 * Component which contains Geometries to render.
 */
public abstract class Renderable extends Component
{
    /**
     * A base color for rendering the component. If none is provided the
     * component should have a default base color.
     */
    private Color myBaseColor;

    /** Geometries which will be rendered. */
    private final Set<Geometry> myGeometries = new HashSet<>();

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public Renderable(Component parent)
    {
        super(parent);
    }

    @Override
    public void clearGeometries()
    {
        myGeometries.clear();
    }

    /**
     * Get the baseColor.
     *
     * @return the baseColor
     */
    public Color getBaseColor()
    {
        return myBaseColor;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        return myGeometries;
    }

    @Override
    public void init()
    {
        myGeometries.clear();
    }

    /**
     * Set the baseColor.
     *
     * @param baseColor the baseColor to set
     */
    public void setBaseColor(Color baseColor)
    {
        myBaseColor = baseColor;
    }
}
