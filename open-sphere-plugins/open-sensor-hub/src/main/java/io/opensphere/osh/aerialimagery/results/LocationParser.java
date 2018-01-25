package io.opensphere.osh.aerialimagery.results;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.model.PropertyInfo;

/**
 * Parses the platform's location data and sets the appropriate values in the
 * respective {@link PlatformMetadata}.
 */
public class LocationParser extends AbstractMetadataParser
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(LocationParser.class);

    /**
     * Constructs a new parser that extracts the vehicles location data.
     *
     * @param uiRegistry Used to notify user of parsing.
     */
    public LocationParser(UIRegistry uiRegistry)
    {
        super(uiRegistry);
    }

    @Override
    protected void parseRows(Output output, List<String> rows, List<PlatformMetadata> metadatas)
    {
        int rowIndex = 0;
        for (String row : rows)
        {
            List<String> values = getTokenizer().tokenize(row);

            double lat = 200;
            double lon = 200;
            double altitude = 0;
            Date time = null;
            int fieldIndex = 0;
            for (String value : values)
            {
                Field field = output.getFields().get(fieldIndex);
                PropertyInfo propertyInfo = PropertyInfo.getProperty(field);
                if (propertyInfo != null)
                {
                    SpecialKey key = propertyInfo.getSpecialKey();
                    if (LatitudeKey.DEFAULT.equals(key))
                    {
                        lat = Double.parseDouble(value);
                    }
                    else if (LongitudeKey.DEFAULT.equals(key))
                    {
                        lon = Double.parseDouble(value);
                    }
                    else if (AltitudeKey.DEFAULT.equals(key))
                    {
                        altitude = Double.parseDouble(value);
                    }
                    else if (TimeKey.DEFAULT.equals(key))
                    {
                        try
                        {
                            time = DateTimeUtilities.parseISO8601Date(value);
                        }
                        catch (ParseException e)
                        {
                            LOGGER.error(e);
                        }
                    }
                }
                fieldIndex++;
            }

            PlatformMetadata metadata = metadatas.get(rowIndex);

            if (lat != 200 && lon != 200)
            {
                metadata.setLocation(LatLonAlt.createFromDegreesMeters(lat, lon, altitude, ReferenceLevel.ELLIPSOID));
            }

            if (time != null)
            {
                metadata.setTime(time);
            }

            rowIndex++;
        }
    }
}
