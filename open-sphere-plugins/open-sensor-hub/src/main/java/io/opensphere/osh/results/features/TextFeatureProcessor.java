package io.opensphere.osh.results.features;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.osh.model.OSHDataTypeInfo;

/** Delimited text FeatureProcessor. */
public class TextFeatureProcessor implements FeatureProcessor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TextFeatureProcessor.class);

    @Override
    public void processData(OSHDataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            Aggregator<List<? extends Serializable>> aggregator)
        throws IOException
    {
        TextDelimitedStringTokenizer tokenizer = new TextDelimitedStringTokenizer(",", "\"");
        MetaDataInfo metaDataInfo = dataType.getMetaDataInfo();
        List<String> keys = metaDataInfo.getKeyNames();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StringUtilities.DEFAULT_CHARSET)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                List<String> tokens = tokenizer.tokenize(line);

                List<Serializable> results = New.list(tokens.size());
                for (int i = 0; i < keys.size(); i++)
                {
                    String key = keys.get(i);
                    String value = tokens.get(i);

                    Serializable valueToUse = value;
                    Class<?> keyClassType = metaDataInfo.getKeyClassType(key);
                    try
                    {
                        if (keyClassType == Double.class)
                        {
                            valueToUse = Double.valueOf(value);
                        }
                        else if (keyClassType == Float.class)
                        {
                            valueToUse = Float.valueOf(value);
                        }
                        else if (keyClassType == Integer.class)
                        {
                            valueToUse = Integer.valueOf(value);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        LOGGER.error(e + ", layer: " + dataType.getTypeName() + ", key: " + key);
                    }

                    results.add(valueToUse);
                }

                aggregator.addItem(results);

                if (ta.isCancelled())
                {
                    stream.cancel();
                    break;
                }
            }

            if (!ta.isCancelled())
            {
                aggregator.processAll();
            }
        }
    }
}
