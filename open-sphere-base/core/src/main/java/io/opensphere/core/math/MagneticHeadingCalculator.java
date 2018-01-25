package io.opensphere.core.math;

import java.util.Calendar;
import java.util.Date;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Calculates the magnetic heading.
 */
public final class MagneticHeadingCalculator
{
    /**
     * The instance of this class.
     */
    private static final MagneticHeadingCalculator ourInstance = new MagneticHeadingCalculator();

    /**
     * The magnetic model used for the calculation.
     */
    private final TSAGeoMag myMagneticModel = new TSAGeoMag();

    /**
     * Not constructible.
     */
    private MagneticHeadingCalculator()
    {
    }

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static MagneticHeadingCalculator getInstance()
    {
        return ourInstance;
    }

    /**
     * Calculates the magnetic heading in degrees.
     *
     * @param trueHeadingDeg The true heading in degrees.
     * @param location The location to calculate magnetic heading at.
     * @param timeline Used to get the year to calculate the
     * @return The magnetic heading in degrees.
     */
    public synchronized double calculateMagneticHeading(double trueHeadingDeg, LatLonAlt location, TimeManager timeline)
    {
        Date endDate = new Date();

        if (timeline != null)
        {
            TimeSpan active = timeline.getActiveTimeSpans().getPrimary().get(0);
            endDate = active.getEndDate();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        int year = calendar.get(Calendar.YEAR);
        double declination = myMagneticModel.getDeclination(location.getLatD(), location.getLonD(), year,
                location.getAltM() + 1000);

        return trueHeadingDeg - declination;
    }
}
