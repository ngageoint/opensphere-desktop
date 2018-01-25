package io.opensphere.osh.aerialimagery.results;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.model.PropertyInfo;

/**
 * Abstract class that knows how to parse orientation data returned from Open
 * Sensor hub.
 */
public abstract class AbstractOrientationParser extends AbstractMetadataParser
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(Logger.class);

    /**
     * Constructor.
     *
     * @param uiRegistry Used to notify user of parsing.
     */
    public AbstractOrientationParser(UIRegistry uiRegistry)
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

            Date time = null;
            double yaw = 500;
            double pitch = 500;
            double roll = 500;
            int fieldIndex = 0;
            for (String value : values)
            {
                Field field = output.getFields().get(fieldIndex);
                PropertyInfo propertyInfo = PropertyInfo.getProperty(field);
                if ("yaw".equals(field.getName()))
                {
                    yaw = Double.parseDouble(value);
                }
                else if ("pitch".equals(field.getName()))
                {
                    pitch = Double.parseDouble(value);
                }
                else if ("roll".equals(field.getName()))
                {
                    roll = Double.parseDouble(value);
                }
                else if (propertyInfo != null && TimeKey.DEFAULT.equals(propertyInfo.getSpecialKey()))
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

                fieldIndex++;
            }

            PlatformMetadata metadata = metadatas.get(rowIndex);
            metadata.setTime(time);

            if (yaw != 500 && pitch != 500 && roll != 500)
            {
                setValues(metadata, yaw, pitch, roll);
            }

            rowIndex++;
        }
    }

    /**
     * Sets the orientation data for the correct item, camera or platform, in
     * the passed in metadata.
     *
     * @param metadata The metadata to set the orientation values for.
     * @param yaw The rotation in degrees of the object looking down on it.
     * @param pitch The rotation in degrees of the object looking at its side.
     * @param roll The rotation in degrees of the object looking at its front.
     */
    protected abstract void setValues(PlatformMetadata metadata, double yaw, double pitch, double roll);
}
