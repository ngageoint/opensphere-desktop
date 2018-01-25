package io.opensphere.csvcommon.common.datetime;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;

/**
 * Interface to an object that provides the configuration of known date formats,
 * and their regular expressions that represent them.
 *
 */
public interface ConfigurationProvider
{
    /**
     * Gets the collection of configured known date formats.
     *
     * @return The collection of known date formats.
     */
    DateFormatsConfig getDateFormats();

    /**
     * Saves the new format to the configuration.
     *
     * @param format The saved format.
     */
    void saveFormat(DateFormat format);

    /**
     * Saves the new formats to the configuration.
     *
     * @param formats The formats to save.
     */
    void saveFormats(Collection<DateFormat> formats);

    /**
     * Gets the column names to exclude.
     *
     * @return The list of partial column names to exclude.
     */
    List<String> getExcludeColumns();
}
