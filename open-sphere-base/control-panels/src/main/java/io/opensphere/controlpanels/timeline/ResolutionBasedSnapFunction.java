package io.opensphere.controlpanels.timeline;

import java.util.function.Supplier;

import io.opensphere.core.util.Constants;

/**
 * Function for calculating a snap destination based on the timeline resolution.
 */
@SuppressWarnings("PMD.UseUtilityClass")
public final class ResolutionBasedSnapFunction extends ModulusBasedSnapFunction
{
    /** Snap to the largest duration that is less than this many pixels. */
    private static final int DEFAULT_SNAP_THRESHOLD_PIXELS = 12;

    // @formatter:off
    /** Some round numbers. */
    private static final int[] MODULI = new int[] {
        Constants.MILLIS_PER_DAY,
        6 * Constants.MILLIS_PER_HOUR,
        3 * Constants.MILLIS_PER_HOUR,
        Constants.MILLIS_PER_HOUR,
        30 * Constants.MILLIS_PER_MINUTE,
        15 * Constants.MILLIS_PER_MINUTE,
        5 * Constants.MILLIS_PER_MINUTE,
        Constants.MILLIS_PER_MINUTE,
        30 * Constants.MILLI_PER_UNIT,
        15 * Constants.MILLI_PER_UNIT,
        5 * Constants.MILLI_PER_UNIT,
        Constants.MILLI_PER_UNIT,
        500,
        100,
        50,
        10,
    };
    // @formatter:on

    /**
     * Get the snap modulus.
     *
     * @param millisPerPixel The number of milliseconds represented by a pixel.
     * @return The modulus.
     */
    public static int getModulus(double millisPerPixel)
    {
        for (int modulus : MODULI)
        {
            double val = modulus / millisPerPixel;
            if (val < DEFAULT_SNAP_THRESHOLD_PIXELS)
            {
                return modulus;
            }
        }
        return 1;
    }

    /**
     * Constructor.
     *
     * @param millisPerPixel The number of milliseconds represented by a pixel.
     */
    public ResolutionBasedSnapFunction(final Supplier<Double> millisPerPixel)
    {
        super(new Supplier<Integer>()
        {
            @Override
            public Integer get()
            {
                return Integer.valueOf(getModulus(millisPerPixel.get().doubleValue()));
            }
        });
    }
}
