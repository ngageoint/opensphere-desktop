package io.opensphere.csvcommon.format;

/**
 * Saves new formats to the system.
 *
 */
@FunctionalInterface
public interface CellFormatSaver
{
    /**
     * Saves a new format.
     *
     * @param format The format to save.
     */
    void saveNewFormat(String format);
}
