package io.opensphere.csvcommon.format.position;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.CellFormatter;

/**
 * Contains base functionality shared by location formatters.
 */
public abstract class BaseLocationFormatter implements CellFormatter
{
    @Override
    public String fromObjectValue(Object value, String format)
    {
        if (value != null)
        {
            return value.toString();
        }
        return null;
    }

    @Override
    public Collection<String> getKnownPossibleFormats()
    {
        return New.list(CoordFormat.DMS.toString(), CoordFormat.DECIMAL.toString(), CoordFormat.DDM.toString());
    }

    @Override
    public String getSystemFormat()
    {
        return null;
    }

    @Override
    public String getFormat(List<String> values)
    {
        // Note:  this needs to be modified to recognize DDMM.MMM format
        // ...
        for (String val : values)
        {
            if (val == null || val.isEmpty())
            {
                continue;
            }
            if (val.chars().anyMatch(ch -> !Character.isDigit(ch) && ch != '.'))
            {
                return CoordFormat.DMS.toString();
            }
            break;
        }

        return CoordFormat.DECIMAL.toString();
    }
}
