package io.opensphere.controlpanels.timeline;

import java.math.RoundingMode;
import java.util.function.Supplier;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Function for calculating a snap destination based on a supplied modulus.
 */
public class ModulusBasedSnapFunction implements SnapFunction
{
    /** The number of milliseconds in the modulus. */
    private final Supplier<Integer> myModulusMillis;

    /**
     * Constructor.
     *
     * @param millis The number of milliseconds in the modulus.
     */
    public ModulusBasedSnapFunction(Supplier<Integer> millis)
    {
        myModulusMillis = Utilities.checkNull(millis, "millis");
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
    {
        Utilities.checkNull(time, "time");

        int modulus = myModulusMillis.get().intValue();
        long timeMillis = time.getEpochMillis();
        long result = timeMillis;
        if (modulus > 1)
        {
            if (mode == RoundingMode.CEILING)
            {
                result = MathUtil.roundUpTo(timeMillis, modulus);
            }
            else if (mode == RoundingMode.FLOOR)
            {
                result = MathUtil.roundDownTo(timeMillis, modulus);
            }
            else if (mode == RoundingMode.HALF_UP)
            {
                result = MathUtil.roundDownTo(timeMillis + modulus / 2, modulus);
            }
            else
            {
                throw new UnexpectedEnumException(mode);
            }
        }
        return TimeInstant.get(result);
    }
}
