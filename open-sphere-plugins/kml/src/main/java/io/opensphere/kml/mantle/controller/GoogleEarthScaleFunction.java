package io.opensphere.kml.mantle.controller;

import java.util.function.Function;

import io.opensphere.core.units.length.Kilometers;

/** Function for scaling things based on the view. */
public class GoogleEarthScaleFunction implements Function<Kilometers, Float>
{
    /** Static instance of the function. */
    public static final GoogleEarthScaleFunction INSTANCE = new GoogleEarthScaleFunction();

    /**
     * Approximate ratio of Google Earth view altitude to OpenSphere view altitude.
     */
    private static final double GE_VIEW_ALTITUDE_RATIO = 0.7;

    /** Approximate factor used in Google Earth's size calculation. */
    private static final double GE_SIZE_FUNCTION_FACTOR = 78018.;

    /** Approximate power value used in Google Earth's size calculation. */
    private static final double GE_SIZE_FUNCTION_POWER = -0.967168;

    /** The viewer value at which the size function equals 32. */
    private static final int MINIMUM_ALTITUDE = 3177;

    /** Number of pixels for a scale of 1. */
    private static final int GE_PIXELS_PER_SCALE = 32;

    @Override
    public Float apply(Kilometers viewAltitude)
    {
        float scale = 1f;
        double googleAlt = viewAltitude.getMagnitude() * GE_VIEW_ALTITUDE_RATIO;
        if (googleAlt > MINIMUM_ALTITUDE)
        {
            int sizePixels = (int)Math.round(GE_SIZE_FUNCTION_FACTOR * Math.pow(googleAlt, GE_SIZE_FUNCTION_POWER));
            scale = (float)sizePixels / GE_PIXELS_PER_SCALE;
        }
        return Float.valueOf(scale);
    }
}
