package io.opensphere.myplaces.export;

import java.io.File;

import io.opensphere.core.export.AbstractExporter;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;

/**
 * Abstract MyPlaces exporter.
 */
public abstract class AbstractMyPlacesExporter extends AbstractExporter
{
    @Override
    public boolean canExport(Class<?> target)
    {
        if (target == null || !File.class.isAssignableFrom(target))
        {
            return false;
        }
        boolean canExport = true;
        for (Object object : getObjects())
        {
            if (!(object instanceof MyPlacesDataGroupInfo || object instanceof MyPlacesDataTypeInfo))
            {
                canExport = false;
                break;
            }
        }
        return canExport;
    }
}
