package io.opensphere.core.hud.framework;

import java.util.Set;

import io.opensphere.core.geometry.Geometry;

/**
 * Describe a border around a component.
 */
public interface Border
{
    /**
     * Pixels occupied by the border at the top.
     *
     * @return Pixels occupied by the border at the top.
     */
    int getBottomInset();

    /**
     * The geometries to be drawn as the border.
     *
     * @return border geometries.
     */
    Set<Geometry> getGeometries();

    /**
     * Pixels occupied by the border at the top.
     *
     * @return Pixels occupied by the border at the top.
     */
    int getLeftInset();

    /**
     * Pixels occupied by the border at the top.
     *
     * @return Pixels occupied by the border at the top.
     */
    int getRightInset();

    /**
     * Pixels occupied by the border at the top.
     *
     * @return Pixels occupied by the border at the top.
     */
    int getTopInset();

    /** Initialize my geometries to be drawn. */
    void init();
}
