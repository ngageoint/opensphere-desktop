package io.opensphere.osh.results.features;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.osh.model.ArrayField;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.model.PropertyInfo;
import io.opensphere.osh.model.VectorField;

/** Binary data FeatureProcessor. This is a work in progress. */
public class BinaryFeatureProcessor implements FeatureProcessor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BinaryFeatureProcessor.class);

    @Override
    public void processData(OSHDataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            Aggregator<List<? extends Serializable>> aggregator)
        throws IOException
    {
        Output resultTemplate = dataType.getResultTemplate(dataType.getOutput());
        boolean hasArray = resultTemplate.getAllFields().stream().anyMatch(f -> f instanceof ArrayField);

        try (DataInputStream dataStream = new DataInputStream(stream))
        {
            while (true)
            {
                List<Serializable> results = New.list();
                for (Field field : resultTemplate.getFields())
                {
                    readField(dataStream, field, results, aggregator);
                }

                if (!hasArray)
                {
                    aggregator.addItem(results);
                }

                if (ta.isCancelled())
                {
                    stream.cancel();
                    break;
                }
            }
        }
        catch (EOFException e)
        {
            LOGGER.debug(e.getMessage());
        }
        finally
        {
            if (!ta.isCancelled())
            {
                aggregator.processAll();
            }
        }
    }

    /**
     * Reads the field (recursively) into either the result or the aggregator.
     *
     * @param dataStream the input stream
     * @param field the field
     * @param results the results (essentially a data element)
     * @param aggregator (receiver for all results)
     * @return true of the field was succesfully read, false otherwise
     * @throws IOException if an problem occurs reading the stream
     */
    private boolean readField(DataInputStream dataStream, Field field, List<Serializable> results,
            Aggregator<List<? extends Serializable>> aggregator)
        throws IOException
    {
        boolean success = true;
        if (field instanceof VectorField)
        {
            for (Field child : ((VectorField)field).getFields())
            {
                success &= readField(dataStream, child, results, aggregator);
                if (!success)
                {
                    break;
                }
            }
        }
        else if (field instanceof ArrayField)
        {
            // should really use the count value, but it's not supplied in the
            // plume layer
            while (true)
            {
                List<Serializable> arrayResults = New.list();
                arrayResults.addAll(results);
                dataStream.mark(8192);

                success &= readField(dataStream, ((ArrayField)field).getField(), arrayResults, aggregator);
                if (!success)
                {
                    dataStream.reset();
                    break;
                }

                aggregator.addItem(arrayResults);
            }
        }
        else
        {
            Serializable rawValue = readValue(dataStream, field);
            if (rawValue != null)
            {
                Serializable value = transformValue(rawValue, field);
                if (value != null)
                {
                    results.add(value);
                }
                else
                {
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Reads a raw value from the input stream.
     *
     * @param dataStream the input stream
     * @param field the field
     * @return the raw value
     * @throws IOException if an problem occurs reading the stream
     */
    private Serializable readValue(DataInputStream dataStream, Field field) throws IOException
    {
        Serializable rawValue = null;
        if (!PropertyInfo.isExcluded(field))
        {
            switch (field.getDataType())
            {
                case "http://www.opengis.net/def/dataType/OGC/0/double":
                    rawValue = Double.valueOf(dataStream.readDouble());
                    break;
                case "http://www.opengis.net/def/dataType/OGC/0/signedInt":
                    rawValue = Integer.valueOf(dataStream.readInt());
                    break;
                default:
            }
        }
        return rawValue;
    }

    /**
     * Transforms the raw value to a value appropriate for mantle.
     *
     * @param rawValue the raw value
     * @param field the field
     * @return the transformed value
     */
    private Serializable transformValue(Serializable rawValue, Field field)
    {
        Serializable value = rawValue;
        PropertyInfo propertyInfo = PropertyInfo.getProperty(field);
        if (propertyInfo != null)
        {
            // Convert to milliseconds since epoch
            if (propertyInfo.getSpecialKey() == TimeKey.DEFAULT)
            {
                value = Long.valueOf((long)(((Number)rawValue).doubleValue() * 1000));
            }
            // Shrink some fields down to save memory
            else if (propertyInfo.getPropertyClass() == Float.class && rawValue.getClass() == Double.class)
            {
                value = Float.valueOf(((Number)rawValue).floatValue());
            }

            if (propertyInfo.getSpecialKey() == LatitudeKey.DEFAULT || propertyInfo.getSpecialKey() == LongitudeKey.DEFAULT)
            {
                double rawDouble = ((Number)rawValue).doubleValue();
                // We read a non lat/lon so we need to break out of this crap
                if (rawDouble > 180)
                {
                    value = null;
                }
            }
        }

        return value;
    }
}
