package io.opensphere.mantle.data.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * Detects column types known to mantle. These detections should be fairly
 * generic since they could apply to any layer.
 */
public class ColumnTypeDetectorImpl implements ColumnTypeDetector
{
    /** The individual detectors. */
    private final List<SpecialColumnDetector> myDetectors = new CopyOnWriteArrayList<>();

    /**
     *
     */
    public ColumnTypeDetectorImpl()
    {
        // TODO Auto-generated constructor stub
    }

    protected ColumnTypeDetectorImpl(ColumnTypeDetectorImpl source)
    {
        // TODO deep copy here
        source.myDetectors.forEach(myDetectors::add);
    }

    /**
     * Adds the supplied detector instance to the set of available detectors.
     *
     * @param detector the detector to add.
     */
    @Override
    public final void addSpecialColumnDetector(SpecialColumnDetector detector)
    {
        myDetectors.add(detector);
    }

    /**
     * Detects column types for the meta data.
     *
     * @param metaData the meta data
     */
    @Override
    public void detectColumnTypes(MetaDataInfo metaData)
    {
        boolean wasDetected = false;

        for (String columnName : metaData.getKeyNames())
        {
            wasDetected |= examineColumn(metaData, columnName);
        }

        if (wasDetected && metaData instanceof DefaultMetaDataInfo)
        {
            ((DefaultMetaDataInfo)metaData).copyKeysToOriginalKeys();
        }

        metaData.setSpecialKeyExaminationRequired(false);
    }

    /**
     * Tests to determine if the supplied column is a special key, and if so,
     * marks it as such in the supplied metadata object.
     *
     * @param metaData the object in which to mark the key's status.
     * @param columnName the column to examine.
     * @return true if the column is a special key, false otherwise.
     */
    @Override
    public boolean examineColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        for (SpecialColumnDetector detector : myDetectors)
        {
            // it's important not to terminate early, as we want all detectors
            // to have a shot at the column.
            wasDetected |= detector.markSpecialColumn(metaData, columnName);
        }
        return wasDetected;
    }

    @Override
    public SpecialKey detectColumn(String columnName)
    {
        SpecialKey specialKey = null;
        for (SpecialColumnDetector detector : myDetectors)
        {
            SpecialKey key = detector.detectColumn(columnName);
            if (key != null)
            {
                specialKey = key;
                break;
            }
        }
        return specialKey;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.ColumnTypeDetector#createCopy()
     */
    @Override
    public ColumnTypeDetector createCopy()
    {
        return new ColumnTypeDetectorImpl(this);
    }
}
