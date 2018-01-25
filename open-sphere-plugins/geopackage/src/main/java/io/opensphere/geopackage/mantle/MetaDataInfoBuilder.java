package io.opensphere.geopackage.mantle;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * Builds {@link MetaDataInfo} for the specified {@link GeoPackageFeatureLayer}.
 */
public class MetaDataInfoBuilder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(MetaDataInfoBuilder.class);

    /**
     * Builds the {@link MetaDataInfo} for the passed in featureLayer.
     *
     * @param featureLayer The layer to build the {@link MetaDataInfo} for.
     * @return The {@link MetaDataInfo} for the layer.
     */
    public MetaDataInfo buildMetaDataInfo(GeoPackageFeatureLayer featureLayer)
    {
        DefaultMetaDataInfo metaInfo = null;

        List<Map<String, Serializable>> data = featureLayer.getData();
        String timeColumn = null;
        if (!data.isEmpty())
        {
            Map<String, Class<?>> columnsToTypes = New.map();

            int index = 0;
            for (Map<String, Serializable> row : data)
            {
                for (Entry<String, Serializable> entry : row.entrySet())
                {
                    timeColumn = findTimeColumn(timeColumn, entry);

                    if (!columnsToTypes.containsKey(entry.getKey()) && entry.getValue() != null)
                    {
                        columnsToTypes.put(entry.getKey(), entry.getValue().getClass());
                    }
                }

                if (columnsToTypes.size() >= row.size())
                {
                    break;
                }

                index++;

                if (index == data.size())
                {
                    for (Entry<String, Serializable> entry : row.entrySet())
                    {
                        if (!columnsToTypes.containsKey(entry.getKey()))
                        {
                            columnsToTypes.put(entry.getKey(), Serializable.class);
                        }
                    }
                }
            }

            metaInfo = new DefaultMetaDataInfo();

            columnsToTypes.put(GeoPackageColumns.GEOMETRY_COLUMN, Geometry.class);

            for (Entry<String, Class<?>> entry : columnsToTypes.entrySet())
            {
                metaInfo.addKey(entry.getKey(), entry.getValue(), this);
            }

            metaInfo.setGeometryColumn(GeoPackageColumns.GEOMETRY_COLUMN);
            if (timeColumn != null)
            {
                metaInfo.setSpecialKey(timeColumn, TimeKey.DEFAULT, this);
            }
        }

        return metaInfo;
    }

    /**
     * Finds the time column if there is one.
     *
     * @param timeColumn The time column to find.
     * @param entry The column and value.
     * @return The time column.
     */
    private String findTimeColumn(String timeColumn, Entry<String, Serializable> entry)
    {
        String newTimeColumn = timeColumn;
        if (!"TIME".equals(timeColumn) && entry.getValue() instanceof String)
        {
            try
            {
                String value = entry.getValue().toString();
                if (value.contains("T") && value.contains("Z"))
                {
                    Date time = DateTimeUtilities.parseISO8601Date(entry.getValue().toString());
                    if (time != null)
                    {
                        newTimeColumn = entry.getKey();
                    }
                }
            }
            catch (ParseException e)
            {
                LOGGER.debug(e, e);
            }
        }

        return newTimeColumn;
    }
}
