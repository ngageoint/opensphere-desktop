package io.opensphere.mantle.data;

/**
 * A contract to detect column types in a data source.
 */
public interface ColumnTypeDetector
{
    /**
     * Adds the supplied detector instance to the set of available detectors.
     *
     * @param detector the detector to add.
     */
    void addSpecialColumnDetector(SpecialColumnDetector detector);

    /**
     * Detects column types for the meta data.
     *
     * @param metaData the meta data
     */
    void detectColumnTypes(MetaDataInfo metaData);

    /**
     * Tests to determine if the supplied column is a special key, and if so,
     * marks it as such in the supplied metadata object.
     *
     * @param metaData the object in which to mark the key's status.
     * @param columnName the column to examine.
     * @return true if the column is a special key, false otherwise.
     */
    boolean examineColumn(MetaDataInfo metaData, String columnName);
}