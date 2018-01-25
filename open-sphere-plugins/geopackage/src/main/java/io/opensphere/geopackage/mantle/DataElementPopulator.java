package io.opensphere.geopackage.mantle;

import java.awt.Color;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * Creates a {@link DataElement} for a geopackage row.
 */
public class DataElementPopulator
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DataElementPopulator.class);

    /** A Counter that helps generate ID's for the geometries. */
    private static AtomicLong ourIDCounter = new AtomicLong(1000000);

    /**
     * Creates {@link MapGeometrySupport} for a geopackage row.
     */
    private final MapGeometryCreator myMapGeometryCreator = new MapGeometryCreator();

    /**
     * Creates {@link MetaDataProvider} for a geopackage row.
     */
    private final MetaDataProviderPopulator myMetaDataPopulator = new MetaDataProviderPopulator();

    /**
     * Creates a {@link DataElement} for the specified geopackage row.
     *
     * @param row The row to create the {@link DataElement} for.
     * @param dataType The {@link DataTypeInfo} the row belongs to.
     * @return The newly created and populated {@link DataElement}.
     */
    public DataElement populateDataElement(Map<String, Serializable> row, DataTypeInfo dataType)
    {
        DataElement dataElement = null;

        MetaDataInfo metaInfo = dataType.getMetaDataInfo();
        MetaDataProvider provider = myMetaDataPopulator.populateProvider(row, metaInfo);
        MapGeometrySupport geometrySupport = myMapGeometryCreator.createGeometrySupport(row);

        Color typeColor = dataType.getBasicVisualizationInfo().getTypeColor();
        String timeColumn = metaInfo.getKeyForSpecialType(TimeKey.DEFAULT);
        TimeSpan timeSpan = TimeSpan.TIMELESS;
        if (timeColumn != null)
        {
            Serializable value = row.get(timeColumn);
            if (value != null)
            {
                String stringValue = value.toString();
                if (stringValue.contains("T") && stringValue.contains("Z"))
                {
                    try
                    {
                        timeSpan = TimeSpan.get(DateTimeUtilities.parseISO8601Date(value.toString()));
                    }
                    catch (ParseException e)
                    {
                        LOGGER.error(e, e);
                    }
                }
            }
        }
        if (geometrySupport != null)
        {
            dataElement = new DefaultMapDataElement(ourIDCounter.incrementAndGet(), timeSpan, dataType, provider,
                    geometrySupport);
            geometrySupport.setColor(typeColor, this);
            geometrySupport.setTimeSpan(timeSpan);
        }
        else
        {
            dataElement = new DefaultDataElement(ourIDCounter.incrementAndGet(), timeSpan, dataType, provider);
        }

        dataElement.getVisualizationState().setColor(typeColor);

        return dataElement;
    }
}
