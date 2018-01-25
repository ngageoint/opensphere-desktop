package io.opensphere.core.geometry.renderproperties;

/**
 * Render properties specific to geometries that will have a base altitude.
 * These geometries can be raised above the surface of the earth.
 */
public interface BaseAltitudeRenderProperties extends ColorRenderProperties
{
    @Override
    BaseAltitudeRenderProperties clone();

    /**
     * Get the base altitude.
     *
     * @return The base altitude.
     */
    float getBaseAltitude();

    /**
     * Set the base altitude.
     *
     * @param baseAlt The base altitude.
     */
    void setBaseAltitude(float baseAlt);
}
