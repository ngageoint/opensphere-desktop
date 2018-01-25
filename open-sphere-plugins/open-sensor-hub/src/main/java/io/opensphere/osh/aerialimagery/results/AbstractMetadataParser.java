package io.opensphere.osh.aerialimagery.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.model.Output;

/**
 * An abstract class that knows how to parse UAV sensor data from open sensor
 * hub.
 */
public abstract class AbstractMetadataParser
{
    /** The tokenizer. */
    private final TextDelimitedStringTokenizer myTokenizer = new TextDelimitedStringTokenizer(",", "\"");

    /**
     * Used to notify user of parsing.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructor.
     *
     * @param uiRegistry Used to notify user of parsing.
     */
    public AbstractMetadataParser(UIRegistry uiRegistry)
    {
        myUIRegistry = uiRegistry;
    }

    /**
     * Parses the platforms locations results.
     *
     * @param output The output describing the fields.
     * @param stream The stream to parse.
     * @param metadatas The list containing the metadatas to populate.
     * @throws IOException If the stream could not be read.
     */
    public void parse(Output output, CancellableInputStream stream, List<PlatformMetadata> metadatas) throws IOException
    {
        List<String> rows = parseResponse(stream);
        initializeMetadatas(metadatas, rows.size());
        parseRows(output, rows, metadatas);
    }

    /**
     * Gets the tokenizer to use to parse a single row into seperate values.
     *
     * @return The tokenizer.
     */
    protected TextDelimitedStringTokenizer getTokenizer()
    {
        return myTokenizer;
    }

    /**
     * Parses the values out of rows and puts them into their respective place
     * in metadatas.
     *
     * @param output Cotains information of the fields within each row.
     * @param rows The rows to parse.
     * @param metadatas The metadatas to put the values into.
     */
    protected abstract void parseRows(Output output, List<String> rows, List<PlatformMetadata> metadatas);

    /**
     * Initializes the metadatas list with empty {@link PlatformMetadata}.
     *
     * @param metadatas The list to populate.
     * @param number The numer of empty metadatas to add.
     */
    private void initializeMetadatas(List<PlatformMetadata> metadatas, int number)
    {
        if (metadatas.isEmpty())
        {
            for (int i = 0; i < number; i++)
            {
                metadatas.add(new PlatformMetadata());
            }
        }
    }

    /**
     * Parses the input stream into java objects.
     *
     * @param stream the input stream to parse
     * @return the java objects
     * @throws IOException if a problem occurred reading the stream
     */
    private List<String> parseResponse(CancellableInputStream stream) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StringUtilities.DEFAULT_CHARSET));
                CancellableTaskActivity ta = CancellableTaskActivity.createActive("Parsing OpenSensorHub results"))
        {
            myUIRegistry.getMenuBarRegistry().addTaskActivity(ta);

            List<String> lines = New.list();
            String line;
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
                if (ta.isCancelled())
                {
                    stream.cancel();
                    break;
                }
            }
            return lines;
        }
    }
}
