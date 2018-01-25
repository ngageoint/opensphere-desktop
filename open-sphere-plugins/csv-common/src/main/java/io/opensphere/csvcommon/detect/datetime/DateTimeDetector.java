package io.opensphere.csvcommon.detect.datetime;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.common.datetime.ConfigurationProviderImpl;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.datetime.algorithm.DateTimeFinder;

/**
 * Detects the date time columns within a csv file.
 */
public class DateTimeDetector implements CellDetector<DateColumnResults>
{
    /**
     * The configuration provider.
     */
    private final ConfigurationProvider myConfigurationProvider;

    /**
     * True if the date rater is just going through the data to detect formats.
     */
    private boolean myIsJustDetectFormats;

    /**
     * Constructs a new DateTimeDetector.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public DateTimeDetector(PreferencesRegistry prefsRegistry)
    {
        myConfigurationProvider = new ConfigurationProviderImpl(prefsRegistry);
    }

    @Override
    public ValuesWithConfidence<DateColumnResults> detect(CellSampler sampler)
    {
        DateTimeFinder finder = new DateTimeFinder(myConfigurationProvider);
        finder.setIsJustDetectFormats(myIsJustDetectFormats);

        return new ValuesWithConfidence<DateColumnResults>(finder.findDates(sampler));
    }

    /**
     * Sets whether or not this date rater should just go through the steps to
     * detect formats, or if it should actually pick date columns.
     *
     * @param isJustFormats True if it should ignore some of the detection rules
     *            in order to find formats.
     */
    public void setIsJustDetectFormats(boolean isJustFormats)
    {
        myIsJustDetectFormats = isJustFormats;
    }
}
