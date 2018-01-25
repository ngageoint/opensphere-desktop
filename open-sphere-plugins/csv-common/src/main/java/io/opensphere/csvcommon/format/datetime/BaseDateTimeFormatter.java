package io.opensphere.csvcommon.format.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.common.datetime.ConfigurationProviderImpl;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.datetime.DateTimeDetector;
import io.opensphere.csvcommon.format.CellFormatSaver;
import io.opensphere.csvcommon.format.CellFormatter;

/**
 * The base formatter for date times.
 *
 */
public abstract class BaseDateTimeFormatter implements CellFormatter, CellFormatSaver
{
    /**
     * The system toolbox.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * The configuration provider.
     */
    private final ConfigurationProvider myProvider;

    /**
     * A cache of date formatters, used to read things faster.
     */
    private Map<String, SimpleDateFormat> myDateFormatters;

    /**
     * Gets the system toolbox.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public BaseDateTimeFormatter(PreferencesRegistry preferencesRegistry)
    {
        myDateFormatters = New.map();
        myPreferencesRegistry = preferencesRegistry;
        myProvider = new ConfigurationProviderImpl(preferencesRegistry);
    }

    @Override
    public Date formatCell(String cellValue, String format) throws ParseException
    {
        Date formattedValue = null;

        String valueToParse = cellValue;

        if (StringUtils.isNotEmpty(format) && format.contains("S"))
        {
            valueToParse = DateTimeUtilities.fixMillis(valueToParse);
        }

        if (StringUtils.isNotEmpty(format))
        {
            formattedValue = getDateParser(format).parse(valueToParse);
        }

        return formattedValue;
    }

    @Override
    public String fromObjectValue(Object value, String format)
    {
        String formattedString = null;

        if (value != null)
        {
            formattedString = getDateParser(format).format(value);
        }

        return formattedString;
    }

    /**
     * Gets the date parser associated with the supplied format.
     *
     * @param pFormat the format to used to parse date strings.
     * @return a Date Formatter to parse date strings.
     */
    protected synchronized SimpleDateFormat getDateParser(String pFormat)
    {
        if (!myDateFormatters.containsKey(pFormat))
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pFormat);
            myDateFormatters.put(pFormat, dateFormat);
        }
        return myDateFormatters.get(pFormat);
    }

    @Override
    public String getFormat(List<String> values)
    {
        DateTimeCellSampler sampler = new DateTimeCellSampler(values);
        DateTimeDetector detector = new DateTimeDetector(myPreferencesRegistry);
        detector.setIsJustDetectFormats(true);

        ValuesWithConfidence<DateColumnResults> results = detector.detect(sampler);
        DateColumnResults result = results.getBestValue();

        String format = null;

        if (result != null && result.getUpTimeColumn() != null && result.getUpTimeColumn().getDateColumnType() == getFormatType())
        {
            format = result.getUpTimeColumn().getPrimaryColumnFormat();
        }

        return format;
    }

    @Override
    public Collection<String> getKnownPossibleFormats()
    {
        DateFormatsConfig config = myProvider.getDateFormats();

        Set<String> knownFormats = New.set();

        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == getFormatType())
            {
                knownFormats.add(format.getSdf());
            }
        }

        return knownFormats;
    }

    @Override
    public String getSystemFormat()
    {
        int timePrecision = myPreferencesRegistry.getPreferences(ListToolPreferences.class)
                .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
        SimpleDateFormat systemFormat = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);

        return systemFormat.toPattern();
    }

    @Override
    public void saveNewFormat(String format)
    {
        DateFormat dateFormat = new DateFormat();
        dateFormat.setType(getFormatType());
        dateFormat.setSdf(format);

        myProvider.saveFormat(dateFormat);
    }

    /**
     * Gets the date format type.
     *
     * @return The type of date format.
     */
    protected abstract Type getFormatType();
}
